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

    public static final String CLUSTER_NOTIFICATION_CONNECTION_FACTORY = "GlobalCacheClusteredConnectionFactory";
    public static final String DELAY_QUEUE_CONNECTION_FACTORY = "GlobalCacheLocalConnectionFactory";
    public static final String NOTIFICATION_TOPIC = "topic/ClusterNotificationTopic";
    public static final String DELAY_QUEUE = "queue/ClusterNotificationDelayQueue";

    public static void sendClusterNotification(CacheInvalidation message) {
        send(message, true, CLUSTER_NOTIFICATION_CONNECTION_FACTORY, NOTIFICATION_TOPIC);
    }

    public static void addToDelayQueue(CacheInvalidation message) {
        send(message, true, DELAY_QUEUE_CONNECTION_FACTORY, DELAY_QUEUE);
    }

    public static List<CacheInvalidation> readFromDelayQueue(int messages) {
        Connection con = null;
        try {
            InitialContext initialContext = new InitialContext();

            ConnectionFactory tcf = (ConnectionFactory)initialContext.lookup(DELAY_QUEUE_CONNECTION_FACTORY);
            con = tcf.createConnection();
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
            return result;
        } catch (NamingException | JMSException e) {
            throw new UnexpectedException(e);
        } finally {
            close(con);
        }
    }

    private static void send(CacheInvalidation message, boolean appendNodeId, String factory, String destination) {
        Connection con = null;
        try {
            message.setSenderId(); // make sure this node id is set in the message

            InitialContext initialContext = new InitialContext();

            ConnectionFactory tcf = (ConnectionFactory)initialContext.lookup(factory);
            con = tcf.createConnection();
            Destination dest = (Destination) initialContext.lookup(destination);
            Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer publisher = session.createProducer(dest);

            BytesMessage bm = session.createBytesMessage();
            if (appendNodeId) {
                bm.writeLong(CacheInvalidation.NODE_ID);
            }
            bm.writeBytes(ObjectCloner.getInstance().toBytesWithClassInfo(message));
            publisher.send(bm);
        } catch (NamingException | JMSException e) {
            throw new UnexpectedException(e);
        } finally {
            close(con);
        }
    }

    private static void close(Connection con) {
        if (con != null) {
            try {
                con.stop();
            } catch (Throwable ignored) { // a dirty hack to work with JavaEE 7. This method is not supported there
            }
            try {
                con.close();
            } catch (Throwable ignored) {
            }
        }
    }
}
