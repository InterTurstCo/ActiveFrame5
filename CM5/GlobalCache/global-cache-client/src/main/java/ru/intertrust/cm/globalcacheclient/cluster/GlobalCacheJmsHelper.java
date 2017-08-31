package ru.intertrust.cm.globalcacheclient.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.model.UnexpectedException;
import ru.intertrust.cm.globalcache.api.util.Size;

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
        try {
            send(message, true, CLUSTER_NOTIFICATION_CONNECTION_FACTORY, NOTIFICATION_TOPIC);
        } catch (Throwable t) {
            logger.error("Exception while sending cluster notification: " + message, t);
            throw t;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Node " + CacheInvalidation.NODE_ID + " (\"this\") sent message to cluster: " + message);
        }
    }

    public static void addToDelayQueue(CacheInvalidation message) {
        try {
            send(message, true, DELAY_QUEUE_CONNECTION_FACTORY, DELAY_QUEUE);
        } catch (Throwable t) {
            logger.error("Exception while adding to delay queue: " + message, t);
            throw t;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Node " + CacheInvalidation.NODE_ID + " (\"this\") added message to own delay queue: " + message);
        }
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
                boolean stopProcessingBatch = false;
                byte[] bytes = null;
                try {
                    final BytesMessage bytesMessage = (BytesMessage) message;
                    final long nodeId = bytesMessage.readLong();
                    if (nodeId != CacheInvalidation.NODE_ID) {
                        logger.error("Received message from another Node's Delay Queue!");
                        continue;
                    }
                    final int messageLength = bytesMessage.readInt();
                    if (messageLength <= 0) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Erroneous message received from Delay Queue of size " + messageLength);
                        }
                        continue;
                    }
                    if (messageLength > 256 * Size.BYTES_IN_MEGABYTE) {
                        logger.warn("Huge message received  from Delay Queue of length: " + messageLength / 1024 / 1024 + " GB. Other messages will wait in the queue");
                        stopProcessingBatch = true;
                    }
                    bytes = new byte[messageLength];
                    bytesMessage.readBytes(bytes);
                    final CacheInvalidation invalidation = ObjectCloner.getInstance().fromBytes(bytes);
                    result.add(invalidation);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Node " + CacheInvalidation.NODE_ID + " (\"this\") read message from own delay queue: " + invalidation);
                    }
                } catch (Throwable throwable) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Erroneous message received from Delay Queue. Unable to deserialize: " + throwable.getMessage());
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Error debug info: ", throwable);
                    }
                }
                if (stopProcessingBatch) {
                    break;
                }
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
            final byte[] messageBytes = ObjectCloner.getInstance().toBytesWithClassInfo(message);
            bm.writeInt(messageBytes.length);
            bm.writeBytes(messageBytes);
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
