package ru.intertrust.cm.test.jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Topic"),
        @ActivationConfigProperty(propertyName="destination", propertyValue="topic/TestTopic")
})
public class TestTopicHandler implements MessageListener {
    final static Logger logger = LoggerFactory.getLogger(TestTopicHandler.class);
    private static long messageCount = 0;

    @Override
    public void onMessage(Message message) {
        if (!(message instanceof ObjectMessage)) {
            return;
        }

        try {            
            ObjectMessage objMessage = (ObjectMessage) message;
            Object object = objMessage.getObject();

            if (!(object instanceof TopicTestMessage)) {
                return;
            }
            TopicTestMessage testMessage = (TopicTestMessage) object;
            
            if (testMessage.getPause() < 0){
                synchronized (TestTopicHandler.class) {
                    messageCount=0;
                }
                logger.info("Clean message counter");
            }else{
                Thread.currentThread().sleep(testMessage.getPause() * 1000);
                synchronized (TestTopicHandler.class) {
                    messageCount++;
                    logger.info("Receive TopicTestMessage. pause=" + testMessage.getPause() + "; Count=" + messageCount);
                }
            }
            
        } catch (Exception e) {
            logger.error("Unexpected exception caught in onMessage", e);
            throw new RuntimeException("Error in TestTopicHandler", e);
        }                
    }

}
