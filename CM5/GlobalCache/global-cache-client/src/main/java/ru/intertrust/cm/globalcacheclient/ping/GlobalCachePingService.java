package ru.intertrust.cm.globalcacheclient.ping;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;
import ru.intertrust.cm.core.business.api.dto.globalcache.PingData;
import ru.intertrust.cm.core.business.api.dto.globalcache.PingRequest;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.globalcacheclient.cluster.GlobalCacheJmsHelper;

/**
 * Сервис проверки отправки и получения уведомлений о сбросе кэша в кластере
 * @author larin
 *
 */
@RestController
public class GlobalCachePingService {
    private static final Logger logger = LoggerFactory.getLogger(GlobalCachePingService.class);
    
    @Autowired
    private Environment environment;
    
    private static Map<String, PingResult> pingResults = new Hashtable<String, PingResult>();
    
    public GlobalCachePingService() {
        logger.info("Init Global Cache Ping Service");
    }
    
    @RequestMapping(value = "/globalcache/ping/{timeout}", method = RequestMethod.GET)
    public PingResult ping(@PathVariable(value = "timeout") Integer timeout) {
        try {
            logger.info("Ping start");

            String nodeName = environment.getProperty("server.name");
            if (nodeName == null) {
                nodeName = "not_config";
            }
            // Создаем объект результата проверки и сохраняем его в глобальной статической мапе
            PingResult result = new PingResult();
            String requestId = UUID.randomUUID().toString();
            pingResults.put(requestId, result);
            result.setRequestId(requestId);
            result.setInitiator(nodeName);
            
            // Формируем ping запрос
            CacheInvalidation pingMessage = new CacheInvalidation();
            pingMessage.setPingData(new PingData());
            pingMessage.getPingData().setRequest(new PingRequest());
            pingMessage.getPingData().getRequest().setNodeName(nodeName);
            pingMessage.getPingData().getRequest().setSendTime(System.currentTimeMillis());
            pingMessage.getPingData().getRequest().setRequestId(requestId);

            //Отправляем ping запрос
            GlobalCacheJmsHelper.sendClusterNotification(pingMessage);
            
            // Ожидаем ответы не более заданного таймаута
            Thread.currentThread().sleep(timeout);
            
            //Удаляем из мапы информацию о запросе
            pingResults.remove(requestId);
            
            //Возвращаем результат
            logger.info("Ping finish");
            return result;
        } catch (Exception ex) {
            throw new FatalException("Error execute ping command", ex);
        }
    }
    
    /**
     * Сохраняем результат ping ответа
     * @param requestId
     * @param nodeInfo
     */
    public static void setPingResult(String requestId, PingNodeInfo nodeInfo) {
        PingResult result = pingResults.get(requestId);
        if (result != null) {
            result.getNodeInfos().add(nodeInfo);
        }
    }

}
