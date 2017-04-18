package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.LocalizableConfig;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.config.eventlog.EventLogsConfig;
import ru.intertrust.cm.core.config.eventlog.LogDomainObjectAccessConfig;
import ru.intertrust.cm.core.config.form.PlainFormBuilder;
import ru.intertrust.cm.core.config.form.impl.PlainFormBuilderImpl;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static ru.intertrust.cm.core.config.NullValues.convertNull;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 *
 * @author vmatsukevich Date: 6/12/13 Time: 5:21 PM
 */
public class ConfigurationExplorerImpl implements ConfigurationExplorer, ApplicationEventPublisherAware {

    private final static Logger logger = LoggerFactory.getLogger(ConfigurationExplorerImpl.class);

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private ConfigurationStorage configStorage;
    private ConfigurationStorageBuilder configurationStorageBuilder;

    private ApplicationEventPublisher applicationEventPublisher;

    private PlainFormBuilder plainFormBuilder;

    private boolean skipLogicalValidation;

    private Configuration configuration;

    @Autowired
    private ApplicationContext context;

    /**
     * Создает {@link ConfigurationExplorerImpl}
     */
    public ConfigurationExplorerImpl(Configuration configuration, boolean skipLogicalValidation) {
        this(configuration, null, skipLogicalValidation);
    }

    public ConfigurationExplorerImpl(Configuration configuration) {
        this(configuration, false);
    }

    public ConfigurationExplorerImpl(Configuration configuration, ApplicationContext context) {
        this(configuration, context, false);
    }

    public ConfigurationExplorerImpl(Configuration configuration, ApplicationContext context, boolean skipLogicalValidation) {
        this.configuration = configuration;
        this.skipLogicalValidation = skipLogicalValidation;
        this.context = context;
        init();
    }

    /**
     * It's a special constructor for Spring, which do not execute init() method which requires ApplicationContext to be set up
     * @param specialSpringConstructor
     * @param configuration
     */
    private ConfigurationExplorerImpl(double specialSpringConstructor, Configuration configuration) {
        this.configuration = configuration;
        this.skipLogicalValidation = false;
    }

    private void init() {
        configStorage = new ConfigurationStorage(configuration);
        configurationStorageBuilder = new ConfigurationStorageBuilder(this, configStorage);
        plainFormBuilder = new PlainFormBuilderImpl(this);
        configurationStorageBuilder.buildConfigurationStorage();
        this.skipLogicalValidation = skipLogicalValidation;
        if (!skipLogicalValidation) {
            validate();
        }
    }

    public ApplicationContext getContext() {
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        lock();
        try {
            return configStorage.configuration;
        } finally {
            unlock();
        }
    }

    public GlobalSettingsConfig getGlobalSettings() {
        lock();
        try {
            return configStorage.globalSettings;
        } finally {
            unlock();
        }
    }

    /**
     * Каждый логический валидатор находится в блоке try/catch для отображения всех ошибок, возникнувших в результате
     * валидации, а не только первого бросившего exception
     */
    private void validate() {
        List<LogicalErrors> logicalErrorsList = new ArrayList<>();

        logicalErrorsList.addAll(new GlobalSettingsLogicalValidator(configStorage.configuration).validate());
        logicalErrorsList.addAll(new DomainObjectLogicalValidator(this).validate());

        if (configStorage.globalSettings.validateAccessMatrices()) {
            logicalErrorsList.addAll(new AccessMatrixLogicalValidator(this).validate());
        }

        logicalErrorsList.addAll(new UniqueNameLogicalValidator(this).validate());

        if (configStorage.globalSettings.validateIndirectPermissions()) {
            logicalErrorsList.addAll(new IndirectlyPermissionLogicalValidator(this).validate());
        }

        if (configStorage.globalSettings.validateAccessMatrices()) {
            logicalErrorsList.addAll(new ReadEvrybodyPermissionLogicalValidator(this).validate());
        }

        if (configStorage.globalSettings.validateGui()) {
            // GUI validation is temporarily switched off as it's not working correctly anymore
            //logicalErrorsList.addAll(validateGui());
        }

        if (!logicalErrorsList.isEmpty()) {
            throw new FatalBeanException("Configuration validation failed",
                    new ConfigurationException(LogicalErrors.toString(logicalErrorsList)));
        }

    }

    public List<LogicalErrors> validateGui() {
        List<LogicalErrors> logicalErrorsList = new ArrayList<>();

        NavigationPanelLogicalValidator navigationPanelLogicalValidator = new NavigationPanelLogicalValidator(this);
        logicalErrorsList.addAll(navigationPanelLogicalValidator.validate());

        FormLogicalValidator formLogicalValidator = new FormLogicalValidator(this);
        logicalErrorsList.addAll(formLogicalValidator.validate());

        logicalErrorsList.addAll(new CollectionViewLogicalValidator(this).validate());

        return logicalErrorsList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfig(Class<T> type, String name) {
        lock();
        try {
            CaseInsensitiveMap<TopLevelConfig> typeMap = configStorage.topLevelConfigMap.get(type);
            if (typeMap == null) {
                return null;
            }

            return(T) typeMap.get(name);
        } finally {
            unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getConfigs(Class<T> type) {
        lock();
        try {
            CaseInsensitiveMap<TopLevelConfig> typeMap = configStorage.topLevelConfigMap.get(type);
            if (typeMap == null) {
                return Collections.EMPTY_LIST;
            }

            //Перекладываем в другой контейнер, для возможности сериализации
            List<T> result = new ArrayList<T>();
            result.addAll((Collection<T>) typeMap.values());
            return result;
        } finally {
            unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Set<Class<?>> getTopLevelConfigClasses() {
        lock();
        try {
            return configStorage.topLevelConfigMap.keySet();
        } finally {
            unlock();
        }
    }

    @Override
    public DomainObjectTypeConfig getDomainObjectTypeConfig(String typeName) {
        return getConfig(DomainObjectTypeConfig.class, typeName);
    }

    @Override
    public Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect) {
        lock();
        try {
            Collection<DomainObjectTypeConfig> childTypes =
                    includeIndirect ? configStorage.indirectChildDomainObjectTypesMap.get(typeName) :
                            configStorage.directChildDomainObjectTypesMap.get(typeName);

            if (childTypes == null) {
                return Collections.emptyList();
            }

            return childTypes;
        } finally {
            unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ReferenceFieldConfig> getReferenceFieldConfigs(String domainObjectConfigName) {
        lock();
        try {
            Set<ReferenceFieldConfig> referenceFieldConfigs = configStorage.referenceFieldsMap.get(domainObjectConfigName);
            if (referenceFieldConfigs != null) {
                return referenceFieldConfigs;
            }
        } finally {
            unlock();
        }

        return configurationStorageBuilder.fillReferenceFieldsMap(domainObjectConfigName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ReferenceFieldConfig> getImmutableReferenceFieldConfigs(String domainObjectConfigName) {
        lock();
        try {
            Set<ReferenceFieldConfig> immutableReferenceFieldConfigs = configStorage.immutableReferenceFieldsMap.get(domainObjectConfigName);
            if (immutableReferenceFieldConfigs != null) {
                return immutableReferenceFieldConfigs;
            }
        } finally {
            unlock();
        }

        configurationStorageBuilder.fillReferenceFieldsMap(domainObjectConfigName);

        lock();
        try {
            return configStorage.immutableReferenceFieldsMap.get(domainObjectConfigName);
        } finally {
            unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        return getFieldConfig(domainObjectConfigName, fieldConfigName, true);
    }

    public Set<String> getDomainObjectTypeAllFieldNamesLowerCased(String doType) {
        return configStorage.fieldNamesLowerCasedByTypeMap.get(doType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName, boolean returnInheritedConfig) {
        lock();

        try {
            if (REFERENCE_TYPE_ANY.equals(domainObjectConfigName)) {
                throw new IllegalArgumentException("'*' is not a valid Domain Object type");
            }

            if (domainObjectConfigName == null || fieldConfigName == null) {
                return null;
            }

            FieldConfigKey fieldConfigKey = new FieldConfigKey(domainObjectConfigName, fieldConfigName, returnInheritedConfig);
            return configStorage.fieldConfigMap.get(fieldConfigKey);
        } finally {
            unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FieldConfig> getDomainObjectTypeMutableFields(String doType, boolean includeInherited)  {
        if (includeInherited) {
            throw new UnsupportedOperationException("Not implemented");
        }
        lock();
        try {
            return configStorage.mutableFieldsNoInheritanceMap.get(doType);
        } finally {
            unlock();
        }
    }

    @Override
    public String getFromHierarchyDomainObjectTypeHavingField(String doType, String fieldName) {
        lock();

        try {
            if (REFERENCE_TYPE_ANY.equals(doType)) {
                throw new IllegalArgumentException("'*' is not a valid Domain Object type");
            }

            if (doType == null) {
                throw new IllegalArgumentException("doType cannot be null");
            }

            if (fieldName == null) {
                throw new IllegalArgumentException("fieldName cannot be null");
            }

            FieldConfigKey fieldConfigKey = new FieldConfigKey(doType, fieldName);
            String result = configStorage.typeInHierarchyHavingFieldMap.get(fieldConfigKey);

            if (result != null) {
                return convertNull(result);
            }
        } finally {
            unlock();
        }

        return convertNull(configurationStorageBuilder.fillTypeInHierarchyHavingField(doType, fieldName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionColumnConfig getCollectionColumnConfig(String collectionViewName, String columnConfigName) {
        lock();
        try {
            FieldConfigKey collectionColumnConfigKey = new FieldConfigKey(collectionViewName, columnConfigName);
            return configStorage.collectionColumnConfigMap.get(collectionColumnConfigKey);
        } finally {
            unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType) {
        lock();
        try {
            List<DynamicGroupConfig> dynamicGroups = configStorage.dynamicGroupConfigByContextMap.get(domainObjectType);
            if (dynamicGroups != null) {
                return dynamicGroups;
            }
        } finally {
            unlock();
        }

        return configurationStorageBuilder.fillDynamicGroupConfigContextMap(domainObjectType);
    }

    /**
     * {@inheritDoc}
     */
    public List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(String trackDOTypeName, String status) {
        lock();
        try {
            FieldConfigKey key = new FieldConfigKey(trackDOTypeName, status);
            List<DynamicGroupConfig> dynamicGroups = configStorage.dynamicGroupConfigsByTrackDOMap.get(key);
            if (dynamicGroups != null) {
                return dynamicGroups;
            }
        } finally {
            unlock();
        }

        return configurationStorageBuilder.fillDynamicGroupConfigsByTrackDOMap(trackDOTypeName, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessMatrixStatusConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status) {
        lock();

        try {
            if (isAuditLogType(domainObjectType)) {
                domainObjectType = getParentTypeOfAuditLog(domainObjectType);
            }

            if (status == null) {
                status = "*";
            }

            FieldConfigKey key = new FieldConfigKey(domainObjectType, status);

            AccessMatrixStatusConfig result = configStorage.accessMatrixByObjectTypeAndStatusMap.get(key);
            if (result != null) {
                return convertNull(result);
            }
        } finally {
            unlock();
        }

        return convertNull(configurationStorageBuilder.fillAccessMatrixByObjectTypeAndStatus(domainObjectType, status));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessMatrixConfig getAccessMatrixByObjectType(String domainObjectType) {
        if (isAuditLogType(domainObjectType)) {
            domainObjectType = getParentTypeOfAuditLog(domainObjectType);
        }

        //Получение конфигурации матрицы, здесь НЕЛЬЗЯ учитывать наследование, так как вызывающие методы должны получить матрицу непосредственно для переданного типа
        return getConfig(AccessMatrixConfig.class, domainObjectType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessMatrixConfig getAccessMatrixByObjectTypeUsingExtension(String domainObjectType) {
        lock();
        try {
            AccessMatrixConfig accessMatrixConfig =
                    configStorage.accessMatrixByObjectTypeUsingExtensionMap.get(domainObjectType);
            if (accessMatrixConfig != null) {
                return convertNull(accessMatrixConfig);
            }
        } finally {
            unlock();
        }

        return convertNull(configurationStorageBuilder.fillAccessMatrixByObjectTypeUsingExtension(domainObjectType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttachmentType(String domainObjectType) {
        lock();
        try {
            return configStorage.attachmentDomainObjectTypes.containsKey(domainObjectType);
        } finally {
            unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuditLogType(String domainObjectType) {
        lock();
        try {
            return configStorage.auditLogTypes.containsKey(domainObjectType);
        } finally {
            unlock();
        }
    }

    @Override
    public String[] getAllAttachmentTypes() {
        lock();
        try {
            Collection<String> values = configStorage.attachmentDomainObjectTypes.values();
            return values.toArray(new String[values.size()]);
        } finally {
            unlock();
        }
    }

    public boolean isReadPermittedToEverybody(String domainObjectType) {
        lock();
        try {
            Boolean result = configStorage.readPermittedToEverybodyMap.get(domainObjectType);
            return result != null ? result : false;
        } finally {
            unlock();
        }
    }

    /**
     * Получение имени типа доменного объекта, который необходимо использовать при вычисление прав на доменный объект в случае
     * использования заимствования прав у связанного объекта
     *
     * @param childTypeName имя типа, для которого необходимо вычислить тип объекта из которого заимствуются права
     * @return имя типа у которого заимствуются права или null в случае если заимствования нет
     */
    @Override
    public String getMatrixReferenceTypeName(String childTypeName) {
        lock();
        try {
            String result = configStorage.matrixReferenceTypeNameMap.get(childTypeName);
            if (result != null) {
                return convertNull(result);
            }
        } finally {
            unlock();
        }

        return convertNull(configurationStorageBuilder.fillMatrixReferenceTypeNameMap(childTypeName));
    }

    /**
     * Получение всех типов, делегирующих проверку прав на объекты данного типа. Включает в результат также
     * типы-наследники, так как проверка прав по ним возлагается на него, и самого себя. Если делегирующих типов нет,
     * нет наследников, и при этом тип сам делегирует проверку собственных прав другому типу, возвращается пустой Set.
     *
     * @param typeName имя типа, для которого необходимо вычислить типы объектов, делегирующих проверку прав на объекты данного типа
     * @return всех типы, делегирующие проверку прав на объекты данного типа
     */
    @Override
    public Set<String> getAllTypesDelegatingAccessCheckTo(String typeName) {
        lock();
        try {
            Set<String> result = configStorage.typesDelegatingAccessCheckTo.get(typeName);
            if (result != null) {
                return result;
            }
        } finally {
            unlock();
        }

        return configurationStorageBuilder.fillTypesDelegatingAccessCheckTo(typeName);
    }

    /**
     * Получение всех типов, делегирующих проверку прав на объекты данного типа, в нижнем регистре. Включает в результат также
     * типы-наследники, так как проверка прав по ним возлагается на него, и самого себя. Если делегирующих типов нет,
     * нет наследников, и при этом тип сам делегирует проверку собственных прав другому типу, возвращается пустой Set.
     *
     * @param typeName имя типа, для которого необходимо вычислить типы объектов, делегирующих проверку прав на объекты данного типа
     * @return всех типы, делегирующие проверку прав на объекты данного типа, в нижнем регистре
     */
    @Override
    public Set<String> getAllTypesDelegatingAccessCheckToInLowerCase(String typeName) {
        lock();
        try {
            Set<String> result = configStorage.typesDelegatingAccessCheckToInLowerCase.get(typeName);
            if (result != null) {
                return result;
            }
        } finally {
            unlock();
        }

        configurationStorageBuilder.fillTypesDelegatingAccessCheckTo(typeName);

        lock();
        try {
            return configStorage.typesDelegatingAccessCheckToInLowerCase.get(typeName);
        } finally {
            unlock();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public ToolBarConfig getDefaultToolbarConfig(String pluginName, String currentLocale) {
        lock();
        try {
            CaseInsensitiveMap<ToolBarConfig> toolbarMap = configStorage.localizedToolbarConfigMap.get(currentLocale);
            if (toolbarMap != null) {
                return toolbarMap.get(pluginName);
            }
            return configStorage.toolbarConfigByPluginMap.get(pluginName);
        } finally {
            unlock();
        }
    }

    @Override
    public String getDomainObjectParentType(String typeName) {
        String[] typesHierarchy = getDomainObjectTypesHierarchy(typeName);

        if (typesHierarchy == null || typesHierarchy.length == 0) {
            return null;
        }

        return typesHierarchy[typesHierarchy.length - 1];
    }

    @Override
    public String getDomainObjectRootType(String typeName) {
        String[] typesHierarchy = getDomainObjectTypesHierarchy(typeName);

        if (typesHierarchy == null || typesHierarchy.length == 0) {
            return typeName;
        }

        return typesHierarchy[0];
    }

    @Override
    public String[] getDomainObjectTypesHierarchy(String typeName) {
        lock();
        try {
            String[] result = this.configStorage.domainObjectTypesHierarchy.get(typeName);
            if (result != null) {
                return result;
            }
        } finally {
            unlock();
        }

        return configurationStorageBuilder.fillDomainObjectTypesHierarchyMap(typeName);
    }

    @Override
    public String[] getDomainObjectTypesHierarchyBeginningFromType(String typeName) {
        lock();
        try {
            String[] result = this.configStorage.domainObjectTypesHierarchyBeginningFromType.get(typeName);
            if (result != null) {
                return result;
            }
        } finally {
            unlock();
        }

        configurationStorageBuilder.fillDomainObjectTypesHierarchyMap(typeName);

        lock();
        try {
            return this.configStorage.domainObjectTypesHierarchyBeginningFromType.get(typeName);
        } finally {
            unlock();
        }
    }

    @Override
    public void updateConfig(TopLevelConfig config) {
        readWriteLock.writeLock().lock();
        try {
            TopLevelConfig oldConfig = getConfig(config.getClass(), config.getName());
            applicationEventPublisher.publishEvent(new ConfigurationUpdateEvent(this, configStorage, oldConfig, config));
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public List<String> getAllowedToCreateUserGroups(String objectType) {
        lock();
        try {
            List<String> userGroups = configStorage.allowedToCreateUserGroupsMap.get(objectType);
            if (userGroups != null) {
                return convertNull(userGroups);
            }
        } finally {
            unlock();
        }

        return convertNull(configurationStorageBuilder.fillAllowedToCreateUserGroups(objectType));
    }

    @Override
    public EventLogsConfig getEventLogsConfiguration() {
        GlobalSettingsConfig globalSettings = getGlobalSettings();
        return globalSettings != null ? globalSettings.getEventLogsConfig() : null;
    }

    @Override
    public LogDomainObjectAccessConfig getDomainObjectAccessEventLogsConfiguration(String typeName) {
        lock();
        try {
            if (this.configStorage.eventLogDomainObjectAccessConfig.containsKey(typeName)) {
                return this.configStorage.eventLogDomainObjectAccessConfig.get(typeName);
            } else {
                return this.configStorage.eventLogDomainObjectAccessConfig.get("*");
            }
        } finally {
            unlock();
        }
    }

    /**
     * {@link ru.intertrust.cm.core.config.ConfigurationExplorer#isAssignable(String, String)}
     */
    @Override
    public boolean isAssignable(String domainObjectType, String assumedDomainObjectType) {
        if (domainObjectType.equalsIgnoreCase(assumedDomainObjectType)) {
            return true;
        }
        String[] parentTypesHierarchy = getDomainObjectTypesHierarchy(domainObjectType);
        if (parentTypesHierarchy == null) {
            return false;
        }

        for (String name : parentTypesHierarchy) {
            if (name.equalsIgnoreCase(assumedDomainObjectType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <T> T getLocalizedConfig(Class<T> type, String name, String currentLocale) {
        if (LocalizableConfig.class.isAssignableFrom(type)) {
            lock();
            try {
                CaseInsensitiveMap<LocalizableConfig> typeMap =
                        configStorage.localizedConfigMap.get(new Pair<String, Class>(currentLocale, type));
                if (typeMap != null) {
                    T config = (T) typeMap.get(name);
                    return config;
                }
            } finally {
                unlock();
            }
        }

        return getConfig(type, name);
    }

    @Override
    public <T> Collection<T> getLocalizedConfigs(Class<T> type, String currentLocale) {
        lock();
        try {
            CaseInsensitiveMap<LocalizableConfig> typeMap = configStorage.localizedConfigMap.get(
                    new Pair<String, Class>(currentLocale, type));
            if (typeMap == null) {
                return Collections.EMPTY_LIST;
            }
            //Перекладываем в другой контейнер, для возможности сериализации
            List<T> result = new ArrayList<T>();
            result.addAll((Collection<T>) typeMap.values());
            return result;
        } finally {
            unlock();
        }
    }

    @Override
    public FormConfig getPlainFormConfig(String name) {
        FormConfig formConfig;

        lock();
        try {
            formConfig = getConfig(FormConfig.class, name);

            if (plainFormBuilder.isRaw(formConfig)) {
                FormConfig formConfigFromCash = configStorage.collectedFormConfigMap.get(name);
                if (formConfigFromCash != null) {
                    return convertNull(formConfigFromCash);
                }
            } else {
                return convertNull(formConfig);
            }
        } finally {
            unlock();
        }

        return convertNull(configurationStorageBuilder.fillPlainFormConfigMap(formConfig, plainFormBuilder));
    }

    @Override
    public FormConfig getLocalizedPlainFormConfig(String name, String currentLocale) {
        FormConfig formConfig = null;

        lock();
        try {
            CaseInsensitiveMap<FormConfig> typeMap = configStorage.localizedCollectedFormConfigMap.get(currentLocale);
            if (typeMap != null) {
                formConfig = typeMap.get(name);
            }
            if (formConfig != null) {
                return convertNull(formConfig);
            }
        } finally {
            unlock();
        }

        return convertNull(configurationStorageBuilder.fillLocalizedPlainFormConfigMap(name, currentLocale));
    }

    public List<FormConfig> getParentFormConfigs(FormConfig formConfig) {
        if (formConfig == null) {
            return Collections.EMPTY_LIST;
        }

        lock();
        try {
            List<FormConfig> parentFormConfigs = new ArrayList<>();
            FormConfig parentFormConfig = getParent(formConfig);
            while (parentFormConfig != null) {
                if (parentFormConfigs.contains(parentFormConfig)) {
                    throw new ConfigurationException(String.format("Loop in the form hierarchy, looped form name is '%s'",
                            parentFormConfig.getName()));
                }
                int index = parentFormConfigs.size() == 0 ? 0 : parentFormConfigs.size() - 1;
                parentFormConfigs.add(index, parentFormConfig);
                parentFormConfig = getParent(parentFormConfig);
            }
            return parentFormConfigs;
        } finally {
            unlock();
        }
    }

    @Override
    public ReentrantReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private FormConfig getParent(FormConfig formConfig) {
        FormConfig result = null;
        if (formConfig.getExtends() != null) {
            result = getConfig(FormConfig.class, formConfig.getExtends());
        }
        return result;
    }

    private String getParentTypeOfAuditLog(String domainObjectType) {
        domainObjectType = domainObjectType.replace(Configuration.AUDIT_LOG_SUFFIX, "");
        return domainObjectType;
    }

    private void lock() {
        readWriteLock.readLock().lock();
    }

    private void unlock() {
        readWriteLock.readLock().unlock();
    }
}
