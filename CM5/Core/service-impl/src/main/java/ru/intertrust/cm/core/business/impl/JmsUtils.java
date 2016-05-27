package ru.intertrust.cm.core.business.impl;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;

/**
 * Utils class for sending JMS messages
 */
public class JmsUtils {

    private static final String CONNECTION_FACTORY = "LocalConnectionFactory";

    public static void sendTopicMessage(Object objectMessage, String queueName) throws JMSException, NamingException {
        TopicConnection topicConn = null;
        try {
            InitialContext initialContext = new InitialContext();

            TopicConnectionFactory tcf = (TopicConnectionFactory)initialContext.lookup(CONNECTION_FACTORY);
            topicConn = tcf.createTopicConnection();
            try {
                topicConn.start();
            } catch (Throwable e) { // a dirty hack to work with JavaEE 7. This method is not supported there
            }
            Topic topic = (Topic) initialContext.lookup(queueName);
            TopicSession topicSession = topicConn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
            topicConn.start();

            TopicPublisher send = topicSession.createPublisher(topic);

            ObjectMessage om = topicSession.createObjectMessage();
            om.setObject((Serializable) objectMessage);
            send.publish(om);
        } finally {
            close(topicConn);
        }
    }

    public static void sendQueueMessage(Object objectMessage, String queueName) throws JMSException, NamingException{
        QueueConnection queueConn = null;
        try {
            InitialContext initialContext = new InitialContext();

            QueueConnectionFactory qcf = (QueueConnectionFactory) initialContext.lookup(CONNECTION_FACTORY);
            queueConn = qcf.createQueueConnection();
            Queue queue = (Queue) initialContext.lookup(queueName);
            QueueSession queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            try {
                queueConn.start();
            } catch (Throwable e) { // a dirty hack to work with JavaEE 7. This method is not supported there
            }

            QueueSender send = queueSession.createSender(queue);
            ObjectMessage om = queueSession.createObjectMessage((Serializable)objectMessage);
            send.send(om);
        } finally {
            close(queueConn);
        }
    }

    private static void close(Connection topicConn) {
        if (topicConn != null) {
            try {
                topicConn.stop();
            } catch (Throwable ignored) { // a dirty hack to work with JavaEE 7. This method is not supported there
            }
            try {
                topicConn.close();
            } catch (Throwable ignored) {
            }
        }
    }
}