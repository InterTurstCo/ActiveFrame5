package ru.intertrust.cm.globalcacheclient.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.model.UnexpectedException;
import ru.intertrust.cm.globalcache.api.CacheInvalidation;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Denis Mitavskiy
 *         Date: 18.04.2016
 *         Time: 18:08
 */
public class GlobalCacheJmsSender {
    final static Logger logger = LoggerFactory.getLogger(GlobalCacheJmsSender.class);

    public static final String CLUSTER_NOTIFICATION_CONNECTION_FACTORY = "RemoteConnectionFactory";
    public static final String DELAY_QUEUE_CONNECTION_FACTORY = "LocalConnectionFactory";
    public static final String NOTIFICATION_TOPIC = "topic/ClusterNotificationTopic";
    public static final String DELAY_QUEUE = "queue/ClusterNotificationDelayQueue";

    public static void sendClusterNotification(CacheInvalidation message) {
        send(message, CLUSTER_NOTIFICATION_CONNECTION_FACTORY, NOTIFICATION_TOPIC);
    }

    public static void addToDelayQueue(CacheInvalidation message) {
        send(message, DELAY_QUEUE_CONNECTION_FACTORY, DELAY_QUEUE);
    }

    private static void send(CacheInvalidation message, String factory, String destination) {
        try {
            long t1 = System.nanoTime();
            InitialContext initialContext = new InitialContext();

            ConnectionFactory tcf = (ConnectionFactory)initialContext.lookup(factory);
            Connection topicConn = tcf.createConnection();
            Destination dest = (Destination) initialContext.lookup(destination);
            Session session = topicConn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer publisher = session.createProducer(dest);
            long t2 = System.nanoTime();

            BytesMessage bm = session.createBytesMessage();
            bm.writeLong(message.getNodeId());
            bm.writeBytes(ObjectCloner.getInstance().toBytesWithClassInfo(message));
            long t3 = System.nanoTime();

            publisher.send(bm);
            long t4 = System.nanoTime();

            publisher.close();

            topicConn.close();
            long t5 = System.nanoTime();
            logger.warn("Publisher time: " + (t2 - t1) / 1000000.0 + "ms");
            logger.warn("Message creation time: " + (t3 - t2) / 1000000.0 + "ms");
            logger.warn("Publish: " + (t4 - t3) / 1000000.0 + "ms");
            logger.warn("Close: " + (t5 - t4) / 1000000.0 + "ms");
        } catch (NamingException | JMSException e) {
            throw new UnexpectedException(e);
        }
    }
}
