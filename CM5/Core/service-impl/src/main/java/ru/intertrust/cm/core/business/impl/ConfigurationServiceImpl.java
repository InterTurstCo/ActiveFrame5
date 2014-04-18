package ru.intertrust.cm.core.business.impl;

import java.util.Collection;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;

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

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private CrudService crudService;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    @Override
    public Configuration getConfiguration() {
        return configurationExplorer.getConfiguration();
    }

    @Override
    public GlobalSettingsConfig getGlobalSettings() {
        return configurationExplorer.getGlobalSettings();
    }

    @Override
    public <T> T getConfig(Class<T> type, String name) {
        return configurationExplorer.getConfig(type, name);
    }

    @Override
    public <T> Collection<T> getConfigs(Class<T> type) {
        return configurationExplorer.getConfigs(type);
    }

    @Override
    public Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect) {
        return configurationExplorer.findChildDomainObjectTypes(typeName, includeIndirect);
    }

    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        return configurationExplorer.getFieldConfig(domainObjectConfigName, fieldConfigName);
    }

    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName, boolean returnInheritedConfig) {
        return configurationExplorer.getFieldConfig(domainObjectConfigName, fieldConfigName, returnInheritedConfig);
    }

    @Override
    public CollectionColumnConfig getCollectionColumnConfig(String collectionConfigName, String columnConfigName) {
        return configurationExplorer.getCollectionColumnConfig(collectionConfigName, columnConfigName);
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType) {
        return configurationExplorer.getDynamicGroupConfigsByContextType(domainObjectType);
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(String objectTypeName, String status) {
        return configurationExplorer.getDynamicGroupConfigsByTrackDO(objectTypeName, status);
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(Id objectId, String status) {
        return configurationExplorer.getDynamicGroupConfigsByTrackDO(crudService.getDomainObjectType(objectId),
                status);
    }

    @Override
    public AccessMatrixStatusConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status) {
        return configurationExplorer.getAccessMatrixByObjectTypeAndStatus(domainObjectType, status);
    }

    /**
     * проверка того, что тип доменного обхекта - Attachment
     * @param domainObjectType тип доменного обхекта
     * @return true если тип доменного обхекта - Attachment
     */
    public boolean isAttachmentType(String domainObjectType) {
        return configurationExplorer.isAttachmentType(domainObjectType);
    }
}
