package ru.intertrust.cm.globalcacheclient.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.model.UnexpectedException;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 18.04.2016
 *         Time: 18:08
 */
public class GlobalCacheJmsHelper {
    final static Logger logger = LoggerFactory.getLogger(GlobalCacheJmsHelper.class);

    public static final String CLUSTER_NOTIFICATION_CONNECTION_FACTORY = "RemoteConnectionFactory";
    public static final String DELAY_QUEUE_CONNECTION_FACTORY = "LocalConnectionFactory";
    public static final String NOTIFICATION_TOPIC = "topic/ClusterNotificationTopic";
    public static final String DELAY_QUEUE = "queue/ClusterNotificationDelayQueue";

    public static void sendClusterNotification(CacheInvalidation message) {
        send(message, true, CLUSTER_NOTIFICATION_CONNECTION_FACTORY, NOTIFICATION_TOPIC);
    }

    public static void addToDelayQueue(CacheInvalidation message) {
        send(message, true, DELAY_QUEUE_CONNECTION_FACTORY, DELAY_QUEUE);
    }

    public static List<CacheInvalidation> readFromDelayQueue(int messages) {
        try {
            InitialContext initialContext = new InitialContext();

            ConnectionFactory tcf = (ConnectionFactory)initialContext.lookup(DELAY_QUEUE_CONNECTION_FACTORY);
            Connection con = tcf.createConnection();
            Destination dest = (Destination) initialContext.lookup(DELAY_QUEUE);
            Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(dest);
            try {
                con.start();
            } catch (Throwable e) { // a dirty hack to work with JavaEE 7. This method is not supported there
            }
            ArrayList<CacheInvalidation> result = new ArrayList<>(messages);
            for (int i = 0; i < messages; ++i) {
                final Message message = consumer.receiveNoWait();
                if (message == null) {
                    break;
                }
                final BytesMessage bytesMessage = (BytesMessage) message;
                final long nodeId = bytesMessage.readLong();
                if (nodeId != CacheInvalidation.NODE_ID) {
                    logger.error("Received message from another Node's Delay Queue!");
                    continue;
                }
                final byte[] bytes = new byte[(int) bytesMessage.getBodyLength() - 4]; // node id is always first
                bytesMessage.readBytes(bytes);
                final CacheInvalidation invalidation = ObjectCloner.getInstance().fromBytes(bytes);
                result.add(invalidation);
            }
            try {
                con.stop();
            } catch (Throwable e) { // a dirty hack to work with JavaEE 7. This method is not supported there
            }
            consumer.close();
            con.close();
            return result;
        } catch (NamingException | JMSException e) {
            throw new UnexpectedException(e);
        }
    }

    private static void send(CacheInvalidation message, boolean appendNodeId, String factory, String destination) {
        try {
            InitialContext initialContext = new InitialContext();

            ConnectionFactory tcf = (ConnectionFactory)initialContext.lookup(factory);
            Connection topicConn = tcf.createConnection();
            Destination dest = (Destination) initialContext.lookup(destination);
            Session session = topicConn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer publisher = session.createProducer(dest);

            BytesMessage bm = session.createBytesMessage();
            if (appendNodeId) {
                bm.writeLong(message.getNodeId());
            }
            bm.writeBytes(ObjectCloner.getInstance().toBytesWithClassInfo(message));
            publisher.send(bm);
            publisher.close();
            topicConn.close();
        } catch (NamingException | JMSException e) {
            throw new UnexpectedException(e);
        }
    }
}
