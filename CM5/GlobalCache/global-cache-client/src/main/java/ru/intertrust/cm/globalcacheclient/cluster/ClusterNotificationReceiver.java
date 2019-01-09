package ru.intertrust.cm.globalcacheclient.cluster;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.globalcache.PingResponse;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcacheclient.ping.GlobalCachePingService;
import ru.intertrust.cm.globalcacheclient.ping.PingNodeInfo;

/**
 * @author Denis Mitavskiy
 *         Date: 18.04.2016
 *         Time: 18:20
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Topic"),
        @ActivationConfigProperty(propertyName="destination", propertyValue= GlobalCacheJmsHelper.NOTIFICATION_TOPIC),
})
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ClusterNotificationReceiver implements MessageListener {
    
    @org.springframework.beans.factory.annotation.Value("${server.name:not_configured}")
    private String nodeName;
    
    @Autowired
    private GlobalCacheClient globalCacheClient;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @Override
    public void onMessage(Message message) {
        try {
            final BytesMessage bytesMessage = (BytesMessage) message;
            final long nodeId = bytesMessage.readLong();
            boolean ownMessage = false;
            if (nodeId == CacheInvalidation.NODE_ID) {
                if (GlobalCacheJmsHelper.logger.isTraceEnabled()) {
                    GlobalCacheJmsHelper.logger.trace("Node " + CacheInvalidation.NODE_ID + " (\"this\") received its own message. Ignoring it. ");
                }
                ownMessage = true;
            }
            final int messageLength = bytesMessage.readInt();
            if (GlobalCacheJmsHelper.logger.isTraceEnabled()) {
                GlobalCacheJmsHelper.logger.trace("Node " + CacheInvalidation.NODE_ID + " (\"this\") received message from cluster node: " + nodeId + ". Message length: " + messageLength + " bytes.");
            }
            if (messageLength <= 0) {
                if (GlobalCacheJmsHelper.logger.isWarnEnabled()) {
                    GlobalCacheJmsHelper.logger.warn("Erroneous message received from cluster of size " + messageLength + ". Ignored.");
                }
                return;
            }
            if (messageLength > 256 * Size.BYTES_IN_MEGABYTE) {
                GlobalCacheJmsHelper.logger.warn("Huge message received from cluster of length: " + messageLength / 1024 / 1024 + " GB. Other messages will wait in the queue");
            }
            final byte[] bytes = new byte[messageLength];
            bytesMessage.readBytes(bytes);
            final CacheInvalidation invalidation = ObjectCloner.getInstance().fromBytes(bytes);
            if (GlobalCacheJmsHelper.logger.isDebugEnabled()) {
                GlobalCacheJmsHelper.logger.debug("Node " + CacheInvalidation.NODE_ID + " (\"this\") parsed message from cluster: " + invalidation);
            }
            
            //Проверка что это ping сообщение
            if (invalidation.getPingData() != null) {
                if (invalidation.getPingData().getResponse() != null) {
                    // Это ping ответ
                    GlobalCacheJmsHelper.logger.info("Reseive ping response message");
                    
                    // Формируем результат
                    PingNodeInfo nodeInfo = new PingNodeInfo();
                    nodeInfo.setNodeName(invalidation.getPingData().getResponse().getNodeName());
                    nodeInfo.setTime(invalidation.getPingData().getResponse().getResponseTime() - invalidation.getPingData().getRequest().getSendTime());
                    
                    // Сохраняем результат
                    GlobalCachePingService.setPingResult(invalidation.getPingData().getRequest().getRequestId(), nodeInfo);
                }else {
                    // Это ping запрос
                    GlobalCacheJmsHelper.logger.info("Reseive ping request message");
                    //Формируем ответ
                    invalidation.getPingData().setResponse(new PingResponse());
                    invalidation.getPingData().getResponse().setResponseTime(System.currentTimeMillis());
                    invalidation.getPingData().getResponse().setNodeName(nodeName);
                    
                    //Отправляем ответ
                    GlobalCacheJmsHelper.sendClusterNotification(invalidation);
                    GlobalCacheJmsHelper.logger.info("Send ping response");
                }
                return;
            }
            
            // Сообщения от самого себя интересны только в случае ping сообщений, остальные игнорируем
            if (ownMessage) {
                return;
            }
            
            if (invalidation.isClearCache()) {
                globalCacheClient.clearCurrentNode();
                return;
            }
            invalidation.setReceiveTime(System.currentTimeMillis());

            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
            final List<DomainObject> domainObjects = domainObjectDao.find(new ArrayList<>(invalidation.getCreatedIdsToInvalidate()), accessToken);
            invalidation.setCreatedDomainObjectsToInvalidate(domainObjects);
            globalCacheClient.invalidateCurrentNode(invalidation);
        } catch (Throwable throwable) {
            if (GlobalCacheJmsHelper.logger.isErrorEnabled()) {
                GlobalCacheJmsHelper.logger.error("Exception caught when processing message from cluster in ClusterNotificationReceiver.onMessage: " + throwable.getMessage() + ". Ignoring message");
            }
            if (GlobalCacheJmsHelper.logger.isDebugEnabled()) {
                GlobalCacheJmsHelper.logger.debug("Exception detail: " + throwable);
            }

        }
    }



}