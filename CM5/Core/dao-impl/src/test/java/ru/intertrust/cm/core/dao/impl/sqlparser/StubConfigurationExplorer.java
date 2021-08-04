package ru.intertrust.cm.core.dao.impl.sqlparser;

import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.eventlog.EventLogsConfig;
import ru.intertrust.cm.core.config.eventlog.LogDomainObjectAccessConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StubConfigurationExplorer implements ConfigurationExplorer {
    @Override
    public Configuration getConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration getDistributiveConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GlobalSettingsConfig getGlobalSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getConfig(Class<T> type, String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Collection<T> getConfigs(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Class<?>> getTopLevelConfigClasses() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DomainObjectTypeConfig getDomainObjectTypeConfig(String typeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ReferenceFieldConfig> getReferenceFieldConfigs(String domainObjectConfigName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ReferenceFieldConfig> getImmutableReferenceFieldConfigs(String domainObjectConfigName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName, boolean returnInheritedConfig) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getDomainObjectTypeAllFieldNamesLowerCased(String doType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FieldConfig> getDomainObjectTypeMutableFields(String doType, boolean includeInherited) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFromHierarchyDomainObjectTypeHavingField(String doType, String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CollectionColumnConfig getCollectionColumnConfig(String collectionViewName, String columnConfigName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(String objectTypeName, String status) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AccessMatrixStatusConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAttachmentType(String domainObjectType) {
        return false;
    }

    @Override
    public boolean isAuditLogType(String domainObjectType) {
        return false;
    }

    @Override
    public String[] getAllAttachmentTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadPermittedToEverybody(String domainObjectType) {
        return false;
    }

    @Override
    public AccessMatrixConfig getAccessMatrixByObjectType(String domainObjectType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AccessMatrixConfig getAccessMatrixByObjectTypeUsingExtension(String domainObjectType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMatrixReferenceTypeName(String childTypeName) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Set getAllTypesDelegatingAccessCheckTo(String typeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getAllTypesDelegatingAccessCheckToInLowerCase(String typeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ToolBarConfig getDefaultToolbarConfig(String pluginName, String currentLocale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDomainObjectParentType(String typeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDomainObjectRootType(String typeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getDomainObjectTypesHierarchy(String typeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getDomainObjectTypesHierarchyBeginningFromType(String typeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateConfig(TopLevelConfig config) {

    }

    @Override
    public List<String> getAllowedToCreateUserGroups(String typeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventLogsConfig getEventLogsConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LogDomainObjectAccessConfig getDomainObjectAccessEventLogsConfiguration(String typeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAssignable(String domainObjectType, String assumedDomainObjectType) {
        return false;
    }

    @Override
    public <T> T getLocalizedConfig(Class<T> type, String name, String locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Collection<T> getLocalizedConfigs(Class<T> type, String locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormConfig getPlainFormConfig(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormConfig getLocalizedPlainFormConfig(String name, String currentLocale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FormConfig> getParentFormConfigs(FormConfig formConfig) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReentrantReadWriteLock getReadWriteLock() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AttachmentTypesConfig getAttachmentTypesConfigWithInherit(DomainObjectTypeConfig domainObjectTypeConfig) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AttachmentTypesConfig getAttachmentTypesConfigWithInherit(String domainObjectTypeName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAttachmentParentType(String attachmentType) {
        throw new UnsupportedOperationException();
   }

}