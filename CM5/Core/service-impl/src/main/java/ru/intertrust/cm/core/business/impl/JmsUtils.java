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
        InitialContext initialContext = new InitialContext();

        TopicConnectionFactory tcf = (TopicConnectionFactory)initialContext.lookup(CONNECTION_FACTORY);
        TopicConnection topicConn = tcf.createTopicConnection();
        Topic topic = (Topic) initialContext.lookup(queueName);
        TopicSession topicSession = topicConn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        topicConn.start();

        TopicPublisher send = topicSession.createPublisher(topic);

        ObjectMessage om = topicSession.createObjectMessage();
        om.setObject((Serializable) objectMessage);
        send.publish(om);
        send.close();

        topicConn.stop();
        topicSession.close();
        topicConn.close();
    }

    public static void sendQueueMessage(Object objectMessage, String queueName) throws JMSException, NamingException{
        InitialContext initialContext = new InitialContext();

        QueueConnectionFactory qcf = (QueueConnectionFactory) initialContext.lookup(CONNECTION_FACTORY);
        QueueConnection queueConn = qcf.createQueueConnection();
        Queue queue = (Queue) initialContext.lookup(queueName);
        QueueSession queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        queueConn.start();

        QueueSender send = queueSession.createSender(queue);
        ObjectMessage om = queueSession.createObjectMessage((Serializable)objectMessage);
        send.send(om);
        send.close();

        queueConn.stop();
        queueSession.close();
        queueConn.close();
    }
}