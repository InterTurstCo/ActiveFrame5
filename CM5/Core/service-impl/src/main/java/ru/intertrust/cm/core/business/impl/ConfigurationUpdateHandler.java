package ru.intertrust.cm.core.business.impl;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.ConfigurationControlService;
import ru.intertrust.cm.core.business.api.dto.CacheInvalidation;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.dao.api.GlobalCacheClient;
import ru.intertrust.cm.core.model.RemoteSuitableException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.HashSet;
import java.util.List;

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

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GlobalCacheClient globalCacheClient;

    @Autowired
    private DomainObjectDao domainObjectDao;

    @Autowired
    private AccessControlService accessControlService;

    @EJB
    private ConfigurationControlService configurationControlService;

    @Override
    public void onMessage(Message message) {
        if (!(message instanceof ObjectMessage)) {
            return;
        }

        try {            
            ObjectMessage objMessage = (ObjectMessage) message;
            Object object = objMessage.getObject();

            if (!(object instanceof ConfigurationUpdateMessage)) {
                return;
            }
            final ConfigurationUpdateMessage configMessage = (ConfigurationUpdateMessage) object;
            if (configMessage.getTopLevelConfig() != null) {
                if (configMessage.fromThisNode()) { // ignore development mode other nodes' messages
                    logger.info("Receive update config message. Config: " + configMessage.getTopLevelConfig());
                    configurationExplorer.updateConfig(configMessage.getTopLevelConfig());
                }
                return;
            }

            if (!configMessage.fromThisNode()) { // vice versa - ignore messages from CURRENT node, as changes have already been made and applied
                invalidateGlobalCacheEntries();
                extensionProcessor().applyConfigurationExtension();
            }
        } catch (JMSException ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    private ConfigurationExtensionProcessor extensionProcessor() {
        final ConfigurationExtensionProcessor configurationExtensionProcessor = (ConfigurationExtensionProcessor) context.getBean("configurationExtensionProcessor");
        configurationExtensionProcessor.setAccessToken(accessControlService.createSystemAccessToken("ConfigurationUpdateHandler"));
        return configurationExtensionProcessor;
    }

    private void invalidateGlobalCacheEntries() {
        final AccessToken systemAccessToken = accessControlService.createSystemAccessToken(this.getClass().getName());
        final List<DomainObject> extensions = domainObjectDao.findAll("configuration_extension", systemAccessToken);
        final List<DomainObject> toolingObjects = domainObjectDao.findAll("config_extension_tooling", systemAccessToken);
        final HashSet<Id> idsToInvalidate = new HashSet<>(extensions.size() + toolingObjects.size());
        for (DomainObject extension : extensions) {
            idsToInvalidate.add(extension.getId());
        }
        for (DomainObject tooling : toolingObjects) {
            idsToInvalidate.add(tooling.getId());
        }
        globalCacheClient.invalidateCurrentNode(new CacheInvalidation(idsToInvalidate, false));
    }
}
