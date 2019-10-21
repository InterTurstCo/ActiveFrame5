package ru.intertrust.cm.core.business.impl;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.AccessMatrixStatusConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.DynamicGroupConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.model.RemoteSuitableException;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link ConfigurationExplorer}
 * @author vmatsukevich
 *         Date: 9/12/13
 *         Time: 10:47 AM
 */
@Stateless
@Local(ConfigurationService.class)
@Remote(ConfigurationService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ConfigurationServiceImpl implements ConfigurationService {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CrudService crudService;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public Configuration getConfiguration() {
        try {
            return configurationExplorer.getConfiguration();
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public GlobalSettingsConfig getGlobalSettings() {
        try {
            return configurationExplorer.getGlobalSettings();
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public <T> T getConfig(Class<T> type, String name) {
        try {
            return configurationExplorer.getConfig(type, name);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public DomainObjectTypeConfig getDomainObjectTypeConfig(String typeName) {
        try {
            return configurationExplorer.getDomainObjectTypeConfig(typeName);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public <T> Collection<T> getConfigs(Class<T> type) {
        try {
            // Перекладываем в другой контейнер для возможности сериализации
            List<T> result = new ArrayList<T>();
            result.addAll(configurationExplorer.getConfigs(type));
            return result;
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect) {
        try {
            return configurationExplorer.findChildDomainObjectTypes(typeName, includeIndirect);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        try {
            return configurationExplorer.getFieldConfig(domainObjectConfigName, fieldConfigName);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName, boolean returnInheritedConfig) {
        try {
            return configurationExplorer.getFieldConfig(domainObjectConfigName, fieldConfigName, returnInheritedConfig);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public CollectionColumnConfig getCollectionColumnConfig(String collectionConfigName, String columnConfigName) {
        try {
            return configurationExplorer.getCollectionColumnConfig(collectionConfigName, columnConfigName);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType) {
        try {
            return configurationExplorer.getDynamicGroupConfigsByContextType(domainObjectType);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(String objectTypeName, String status) {
        try {
            return configurationExplorer.getDynamicGroupConfigsByTrackDO(objectTypeName, status);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(Id objectId, String status) {
        try {
            return configurationExplorer.getDynamicGroupConfigsByTrackDO(crudService.getDomainObjectType(objectId),
                    status);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public AccessMatrixStatusConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status) {
        try {
            return configurationExplorer.getAccessMatrixByObjectTypeAndStatus(domainObjectType, status);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * проверка того, что тип доменного обхекта - Attachment
     * @param domainObjectType тип доменного обхекта
     * @return true если тип доменного обхекта - Attachment
     */
    public boolean isAttachmentType(String domainObjectType) {
        try {
            return configurationExplorer.isAttachmentType(domainObjectType);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ToolBarConfig getDefaultToolbarConfig(String pluginName, String currentLocale) {
        try {
            return configurationExplorer.getDefaultToolbarConfig(pluginName, currentLocale);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDomainObjectParentType(String typeName) {
        try {
            return configurationExplorer.getDomainObjectParentType(typeName);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDomainObjectRootType(String typeName) {
        try {
            return configurationExplorer.getDomainObjectRootType(typeName);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] getDomainObjectTypesHierarchy(String typeName) {
        try {
            return configurationExplorer.getDomainObjectTypesHierarchy(typeName);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public <T> T getLocalizedConfig(Class<T> type, String name, String locale) {
        try {
            return configurationExplorer.getLocalizedConfig(type, name, locale);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }

    @Override
    public <T> Collection<T> getLocalizedConfigs(Class<T> type, String locale) {
        try {
            return configurationExplorer.getLocalizedConfigs(type, locale);
        } catch (Exception ex) {
            throw RemoteSuitableException.convert(ex);
        }
    }
}
