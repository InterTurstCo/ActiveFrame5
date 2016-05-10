package ru.intertrust.cm.globalcacheclient.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * @author Denis Mitavskiy
 *         Date: 18.04.2016
 *         Time: 18:20
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Topic"),
        @ActivationConfigProperty(propertyName="destination", propertyValue= GlobalCacheJmsHelper.NOTIFICATION_TOPIC),
})
public class ClusterNotificationReceiver implements MessageListener {
    final static Logger logger = LoggerFactory.getLogger(ClusterNotificationReceiver.class);

    @Override
    public void onMessage(Message message) {
        try {
            final BytesMessage bytesMessage = (BytesMessage) message;
            final long nodeId = bytesMessage.readLong();
            if (nodeId == CacheInvalidation.NODE_ID) {
                return;
            }
            final byte[] bytes = new byte[(int) bytesMessage.getBodyLength() - 4]; // node id is always first
            bytesMessage.readBytes(bytes);
            final CacheInvalidation invalidation = ObjectCloner.getInstance().fromBytes(bytes);
            invalidation.setReceiveTime(System.currentTimeMillis());
            GlobalCacheJmsHelper.addToDelayQueue(invalidation);
        } catch (Exception e) {
            logger.error("Unexpected exception caught in onMessage", e);
            throw new RuntimeException("Error in Notification Receiver", e);
        }
    }



}