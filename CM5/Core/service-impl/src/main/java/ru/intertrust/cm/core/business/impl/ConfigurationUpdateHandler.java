package ru.intertrust.cm.core.business.impl;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.model.UnexpectedException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * Java Message Driven Bean for Configuration updates processing (updating caches, etc.)
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Topic"),
        @ActivationConfigProperty(propertyName="destination", propertyValue="topic/ConfigurationUpdateTopic")
})
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ConfigurationUpdateHandler implements MessageListener {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(ConfigurationUpdateHandler.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    public void onMessage(Message message) {
        if (!(message instanceof ObjectMessage)) {
            return;
        }

        try {            
            ObjectMessage objMessage = (ObjectMessage) message;
            Object object = objMessage.getObject();

            if (!(object instanceof TopLevelConfig)) {
                return;
            }
            logger.info("Receive update config message. Config: " + object);
            
            TopLevelConfig config = (TopLevelConfig) object;
            configurationExplorer.updateConfig(config);
        } catch (JMSException e) {
            logger.error("Unexpected exception caught in onMessage", e);
            throw new UnexpectedException("ConfigurationUpdateHandler", "onMessage", "", e);
        }
    }
}
