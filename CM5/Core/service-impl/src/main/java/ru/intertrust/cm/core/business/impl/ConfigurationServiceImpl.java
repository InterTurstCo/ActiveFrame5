package ru.intertrust.cm.core.business.impl;

import java.util.Collection;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.model.UnexpectedException;

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
    private CrudService crudService;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public Configuration getConfiguration() {
        try {
            return configurationExplorer.getConfiguration();
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getConfiguration", ex);
            throw new UnexpectedException("ConfigurationService", "getConfiguration", "", ex);
        }
    }

    @Override
    public GlobalSettingsConfig getGlobalSettings() {
        try {
            return configurationExplorer.getGlobalSettings();
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getGlobalSettings", ex);
            throw new UnexpectedException("ConfigurationService", "getGlobalSettings", "", ex);
        }
    }

    @Override
    public <T> T getConfig(Class<T> type, String name) {
        try {
            return configurationExplorer.getConfig(type, name);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getConfig", ex);
            throw new UnexpectedException("ConfigurationService", "getConfig",
                    "type:" + type + " name:" + name, ex);
        }
    }

    @Override
    public DomainObjectTypeConfig getDomainObjectTypeConfig(String typeName) {
        try {
            return configurationExplorer.getDomainObjectTypeConfig(typeName);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDomainObjectTypeConfig", ex);
            throw new UnexpectedException("ConfigurationService", "getDomainObjectTypeConfig", "type:" + typeName, ex);
        }
    }

    @Override
    public <T> Collection<T> getConfigs(Class<T> type) {
        try {
            return configurationExplorer.getConfigs(type);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getConfigs", ex);
            throw new UnexpectedException("ConfigurationService", "getConfigs",
                    "type:" + type, ex);
        }
    }

    @Override
    public Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect) {
        try {
            return configurationExplorer.findChildDomainObjectTypes(typeName, includeIndirect);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in findChildDomainObjectTypes", ex);
            throw new UnexpectedException("ConfigurationService", "findChildDomainObjectTypes",
                    "typeName:" + typeName + " includeIndirect:" + includeIndirect, ex);
        }
    }

    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        try {
            return configurationExplorer.getFieldConfig(domainObjectConfigName, fieldConfigName);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getFieldConfig", ex);
            throw new UnexpectedException("ConfigurationService", "getFieldConfig",
                    "domainObjectConfigName:" + domainObjectConfigName + " fieldConfigName:" + fieldConfigName, ex);
        }
    }

    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName, boolean returnInheritedConfig) {
        try {
            return configurationExplorer.getFieldConfig(domainObjectConfigName, fieldConfigName, returnInheritedConfig);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getFieldConfig", ex);
            throw new UnexpectedException("ConfigurationService", "getFieldConfig",
                    "domainObjectConfigName:" + domainObjectConfigName + " fieldConfigName:" + fieldConfigName
                    + " returnInheritedConfig:" + returnInheritedConfig, ex);
        }
    }

    @Override
    public CollectionColumnConfig getCollectionColumnConfig(String collectionConfigName, String columnConfigName) {
        try {
            return configurationExplorer.getCollectionColumnConfig(collectionConfigName, columnConfigName);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getCollectionColumnConfig", ex);
            throw new UnexpectedException("ConfigurationService", "getCollectionColumnConfig",
                    "collectionConfigName:" + collectionConfigName + " columnConfigName:" + columnConfigName, ex);
        }
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType) {
        try {
            return configurationExplorer.getDynamicGroupConfigsByContextType(domainObjectType);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDynamicGroupConfigsByContextType", ex);
            throw new UnexpectedException("ConfigurationService", "getDynamicGroupConfigsByContextType",
                    "domainObjectType:" + domainObjectType, ex);
        }
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(String objectTypeName, String status) {
        try {
            return configurationExplorer.getDynamicGroupConfigsByTrackDO(objectTypeName, status);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDynamicGroupConfigsByTrackDO", ex);
            throw new UnexpectedException("ConfigurationService", "getDynamicGroupConfigsByTrackDO",
                    "objectTypeName:" + objectTypeName + " status:" + status, ex);
        }
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(Id objectId, String status) {
        try {
            return configurationExplorer.getDynamicGroupConfigsByTrackDO(crudService.getDomainObjectType(objectId),
                    status);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDynamicGroupConfigsByTrackDO", ex);
            throw new UnexpectedException("ConfigurationService", "getDynamicGroupConfigsByTrackDO",
                    "objectId:" + objectId + " status:" + status, ex);
        }
    }

    @Override
    public AccessMatrixStatusConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status) {
        try {
            return configurationExplorer.getAccessMatrixByObjectTypeAndStatus(domainObjectType, status);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getAccessMatrixByObjectTypeAndStatus", ex);
            throw new UnexpectedException("ConfigurationService", "getAccessMatrixByObjectTypeAndStatus",
                    "domainObjectType:" + domainObjectType + " status:" + status, ex);
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
            logger.error("Unexpected exception caught in isAttachmentType", ex);
            throw new UnexpectedException("ConfigurationService", "isAttachmentType",
                    "domainObjectType:" + domainObjectType, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ToolBarConfig getDefaultToolbarConfig(String pluginName) {
        try {
            return configurationExplorer.getDefaultToolbarConfig(pluginName);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDefaultToolbarConfig", ex);
            throw new UnexpectedException("ConfigurationService", "getDefaultToolbarConfig",
                    "pluginName:" + pluginName, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDomainObjectParentType(String typeName) {
        try {
            return configurationExplorer.getDomainObjectParentType(typeName);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDomainObjectParentType", ex);
            throw new UnexpectedException("ConfigurationService", "getDomainObjectParentType",
                    "typeName:" + typeName, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDomainObjectRootType(String typeName) {
        try {
            return configurationExplorer.getDomainObjectRootType(typeName);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDomainObjectRootType", ex);
            throw new UnexpectedException("ConfigurationService", "getDomainObjectRootType",
                    "typeName:" + typeName, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] getDomainObjectTypesHierarchy(String typeName) {
        try {
            return configurationExplorer.getDomainObjectTypesHierarchy(typeName);
        } catch (Exception ex) {
            logger.error("Unexpected exception caught in getDomainObjectTypesHierarchy", ex);
            throw new UnexpectedException("ConfigurationService", "getDomainObjectTypesHierarchy",
                    "typeName:" + typeName, ex);
        }
    }
}
