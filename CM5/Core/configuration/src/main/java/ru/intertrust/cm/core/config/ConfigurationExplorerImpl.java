package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.util.ObjectCloner;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 *
 * @author vmatsukevich Date: 6/12/13 Time: 5:21 PM
 */
public class ConfigurationExplorerImpl implements ConfigurationExplorer, ApplicationEventPublisherAware {

    private final static Logger logger = LoggerFactory.getLogger(ConfigurationExplorerImpl.class);

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private ConfigurationStorage configStorage;
    private ConfigurationStorageBuilder configurationStorageBuilder;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private FormLogicalValidator formLogicalValidator;

    @Autowired
    private NavigationPanelLogicalValidator navigationPanelLogicalValidator;

    @Autowired
    private PlainFormBuilder plainFormBuilder;

    //private ObjectCloner objectCloner = new ObjectCloner();

    /**
     * Создает {@link ConfigurationExplorerImpl}
     */
    public ConfigurationExplorerImpl(Configuration configuration, boolean skipLogicalValidation) {
        configStorage = new ConfigurationStorage(configuration);
        configurationStorageBuilder = new ConfigurationStorageBuilder(this, configStorage);

        configurationStorageBuilder.buildConfigurationStorage();
        if (!skipLogicalValidation) {
            validate();
        }

    }

    public ConfigurationExplorerImpl(Configuration configuration) {
        this(configuration, false);
    }

    private void init() {
        if (configStorage.globalSettings.validateGui()) {
            validateGui();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        readLock.lock();
        try {
            return getReturnObject(configStorage.configuration);
        } finally {
            readLock.unlock();
        }
    }

    public GlobalSettingsConfig getGlobalSettings() {
        readLock.lock();
        try {
            return getReturnObject(configStorage.globalSettings);
        } finally {
            readLock.unlock();
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

        if (!logicalErrorsList.isEmpty()) {
            throw new FatalBeanException("Configuration validation failed",
                    new ConfigurationException(LogicalErrors.toString(logicalErrorsList)));
        }
    }

    public void validateGui() {
        List<LogicalErrors> logicalErrorsList = new ArrayList<>();

        navigationPanelLogicalValidator.setConfigurationExplorer(this);
        logicalErrorsList.addAll(navigationPanelLogicalValidator.validate());

        formLogicalValidator.setConfigurationExplorer(this);
        logicalErrorsList.addAll(formLogicalValidator.validate());

        logicalErrorsList.addAll(new CollectionViewLogicalValidator(this).validate());

        if (!logicalErrorsList.isEmpty()) {
            String errorMessage = LogicalErrors.toString(logicalErrorsList);
            if (!errorMessage.isEmpty()) {
                throw new ConfigurationException(LogicalErrors.toString(logicalErrorsList));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfig(Class<T> type, String name) {
        readLock.lock();
        try {
            CaseInsensitiveMap<TopLevelConfig> typeMap = configStorage.topLevelConfigMap.get(type);
            if (typeMap == null) {
                return null;
            }

            T config = (T) typeMap.get(name);
            return getReturnObject(config);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getConfigs(Class<T> type) {
        readLock.lock();
        try {
            CaseInsensitiveMap<TopLevelConfig> typeMap = configStorage.topLevelConfigMap.get(type);
            if (typeMap == null) {
                return Collections.EMPTY_LIST;
            }

            //Перекладываем в другой контейнер, для возможности сериализации
            List<T> result = new ArrayList<T>();
            result.addAll((Collection<T>) typeMap.values());

            return getReturnObject(result);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Set<Class<?>> getTopLevelConfigClasses() {
        readLock.lock();
        try {
            return configStorage.topLevelConfigMap.keySet();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public DomainObjectTypeConfig getDomainObjectTypeConfig(String typeName) {
        readLock.lock();
        try {
            return getConfig(DomainObjectTypeConfig.class, typeName);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect) {
        readLock.lock();
        try {
            Collection<DomainObjectTypeConfig> childTypes =
                    includeIndirect ? configStorage.indirectChildDomainObjectTypesMap.get(typeName) :
                            configStorage.directChildDomainObjectTypesMap.get(typeName);

            if (childTypes == null) {
                return Collections.emptyList();
            }

            return getReturnObject(childTypes);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        readLock.lock();
        try {
            return getFieldConfig(domainObjectConfigName, fieldConfigName, true);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ReferenceFieldConfig> getReferenceFieldConfigs(String domainObjectConfigName) {
        readLock.lock();
        try {
            Set<ReferenceFieldConfig> referenceFieldConfigs = configStorage.referenceFieldsMap.get(domainObjectConfigName);
            if (referenceFieldConfigs == null) {
                referenceFieldConfigs = configurationStorageBuilder.fillReferenceFieldsMap(domainObjectConfigName);
            }

            return getReturnObject(referenceFieldConfigs);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ReferenceFieldConfig> getImmutableReferenceFieldConfigs(String domainObjectConfigName) {
        readLock.lock();
        try {
            Set<ReferenceFieldConfig> immutableReferenceFieldConfigs = configStorage.immutableReferenceFieldsMap.get(domainObjectConfigName);
            if (immutableReferenceFieldConfigs == null) {
                configurationStorageBuilder.fillReferenceFieldsMap(domainObjectConfigName);
                immutableReferenceFieldConfigs = configStorage.immutableReferenceFieldsMap.get(domainObjectConfigName);
            }

            return getReturnObject(immutableReferenceFieldConfigs);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName, boolean returnInheritedConfig) {
        readLock.lock();
        try {
            if (REFERENCE_TYPE_ANY.equals(domainObjectConfigName)) {
                throw new IllegalArgumentException("'*' is not a valid Domain Object type");
            }
            if (domainObjectConfigName == null || fieldConfigName == null) {
                return null;
            }

            FieldConfigKey fieldConfigKey = new FieldConfigKey(domainObjectConfigName, fieldConfigName);
            FieldConfig result = configStorage.fieldConfigMap.get(fieldConfigKey);

            if (result != null) {
                return getReturnObject(result);
            }

            if (returnInheritedConfig) {
                DomainObjectTypeConfig domainObjectTypeConfig =
                        getConfig(DomainObjectTypeConfig.class, domainObjectConfigName);
                if (domainObjectTypeConfig == null) {
                    return null;
                }
                if (domainObjectTypeConfig.getExtendsAttribute() != null) {
                    return getFieldConfig(domainObjectTypeConfig.getExtendsAttribute(), fieldConfigName);
                }
            }

            return null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionColumnConfig getCollectionColumnConfig(String collectionViewName, String columnConfigName) {
        readLock.lock();
        try {
            FieldConfigKey collectionColumnConfigKey = new FieldConfigKey(collectionViewName, columnConfigName);
            CollectionColumnConfig collectionColumnConfig = configStorage.collectionColumnConfigMap.get(collectionColumnConfigKey);
            return getReturnObject(collectionColumnConfig);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType) {
        readLock.lock();
        try {
            List<DynamicGroupConfig> dynamicGroups = configStorage.dynamicGroupConfigByContextMap.get(domainObjectType);
            if (dynamicGroups == null) {
                dynamicGroups = configurationStorageBuilder.fillDynamicGroupConfigContextMap(domainObjectType);
            }

            return getReturnObject(dynamicGroups);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(String trackDOTypeName, String status) {
        readLock.lock();
        try {
            FieldConfigKey key = new FieldConfigKey(trackDOTypeName, status);
            List<DynamicGroupConfig> dynamicGroups = configStorage.dynamicGroupConfigsByTrackDOMap.get(key);
            if (dynamicGroups == null) {
                dynamicGroups = configurationStorageBuilder.fillDynamicGroupConfigsByTrackDOMap(trackDOTypeName, status);
            }

            return getReturnObject(dynamicGroups);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessMatrixStatusConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status) {
        readLock.lock();
        try {
            if (isAuditLogType(domainObjectType)) {
                domainObjectType = getParentTypeOfAuditLog(domainObjectType);
            }

            if (status == null) {
                status = "*";
            }

            FieldConfigKey key = new FieldConfigKey(domainObjectType, status);
            AccessMatrixStatusConfig result = configStorage.accessMatrixByObjectTypeAndStatusMap.get(key);

            if (result == null) {
                result = configurationStorageBuilder.fillAccessMatrixByObjectTypeAndStatus(domainObjectType, status);
            }

            if (NullValues.isNull(result)) {
                return null;
            }

            return getReturnObject(result);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessMatrixConfig getAccessMatrixByObjectType(String domainObjectType) {
        readLock.lock();
        try {
            if (isAuditLogType(domainObjectType)) {
                domainObjectType = getParentTypeOfAuditLog(domainObjectType);
            }
            //Получение конфигурации матрицы, здесь НЕЛЬЗЯ учитывать наследование, так как вызывающие методы должны получить матрицу непосредственно для переданного типа
            AccessMatrixConfig accessMatrixConfig = getConfig(AccessMatrixConfig.class, domainObjectType);
            return getReturnObject(accessMatrixConfig);
        } finally {
            readLock.unlock();
        }
    }

    private String getParentTypeOfAuditLog(String domainObjectType) {
        domainObjectType = domainObjectType.replace(Configuration.AUDIT_LOG_SUFFIX, "");
        return domainObjectType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessMatrixConfig getAccessMatrixByObjectTypeUsingExtension(String domainObjectType) {
        readLock.lock();
        try {
            if (isAuditLogType(domainObjectType)) {
                domainObjectType = getParentTypeOfAuditLog(domainObjectType);
            }

            AccessMatrixConfig accessMatrixConfig = getConfig(AccessMatrixConfig.class, domainObjectType);

            if (accessMatrixConfig != null) {
                return getReturnObject(accessMatrixConfig);
            } else {
                DomainObjectTypeConfig domainObjectTypeConfig =
                        getConfig(DomainObjectTypeConfig.class, domainObjectType);
                if (domainObjectTypeConfig != null && domainObjectTypeConfig.getExtendsAttribute() != null) {
                    String parentDOType = domainObjectTypeConfig.getExtendsAttribute();
                    return getAccessMatrixByObjectTypeUsingExtension(parentDOType);
                }
            }
        } finally {
            readLock.unlock();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttachmentType(String domainObjectType) {
        readLock.lock();
        try {
            return configStorage.attachmentDomainObjectTypes.containsKey(domainObjectType);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuditLogType(String domainObjectType) {
        readLock.lock();
        try {
            return configStorage.auditLogTypes.containsKey(domainObjectType);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String[] getAllAttachmentTypes() {
        readLock.lock();
        try {
            Collection<String> values = configStorage.attachmentDomainObjectTypes.values();
            return values.toArray(new String[values.size()]);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isReadPermittedToEverybody(String domainObjectType) {
        readLock.lock();
        try {
            Boolean result = configStorage.readPermittedToEverybodyMap.get(domainObjectType);
            return result != null ? result : false;
        } finally {
            readLock.unlock();
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
        readLock.lock();
        try {
            String result = configStorage.matrixReferenceTypeNameMap.get(childTypeName);

            if (result == null) {
                result = configurationStorageBuilder.fillMatrixReferenceTypeNameMap(childTypeName);
            }

            if (NullValues.isNull(result)) {
                return null;
            }

            return getReturnObject(result);
        } finally {
            readLock.unlock();
        }
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
        readLock.lock();
        try {
            Set<String> result = configStorage.typesDelegatingAccessCheckTo.get(typeName);

            if (result == null) {
                configurationStorageBuilder.fillTypesDelegatingAccessCheckTo(typeName);
                result = configStorage.typesDelegatingAccessCheckTo.get(typeName);
            }

            return getReturnObject(result);
        } finally {
            readLock.unlock();
        }
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
        readLock.lock();
        try {
            Set<String> result = configStorage.typesDelegatingAccessCheckToInLowerCase.get(typeName);

            if (result == null) {
                configurationStorageBuilder.fillTypesDelegatingAccessCheckTo(typeName);
                result = configStorage.typesDelegatingAccessCheckToInLowerCase.get(typeName);
            }

            return getReturnObject(result);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public ToolBarConfig getDefaultToolbarConfig(String pluginName, String currentLocale) {
        readLock.lock();
        try {
            CaseInsensitiveMap<ToolBarConfig> toolbarMap = configStorage.localizedToolbarConfigMap.get(currentLocale);
            if (toolbarMap != null) {
                ToolBarConfig toolBarConfig = toolbarMap.get(pluginName);
                return getReturnObject(toolBarConfig);
            }
            ToolBarConfig toolBarConfig = configStorage.toolbarConfigByPluginMap.get(pluginName);
            return getReturnObject(toolBarConfig);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getDomainObjectParentType(String typeName) {
        readLock.lock();
        try {
            String[] typesHierarchy = getDomainObjectTypesHierarchy(typeName);

            if (typesHierarchy == null || typesHierarchy.length == 0) {
                return null;
            }

            return typesHierarchy[typesHierarchy.length - 1];
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getDomainObjectRootType(String typeName) {
        readLock.lock();
        try {
            String[] typesHierarchy = getDomainObjectTypesHierarchy(typeName);

            if (typesHierarchy == null || typesHierarchy.length == 0) {
                return typeName;
            }

            return typesHierarchy[0];
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String[] getDomainObjectTypesHierarchy(String typeName) {
        readLock.lock();
        try {
            String[] result = this.configStorage.domainObjectTypesHierarchy.get(typeName);
            if (result == null) {
                result = configurationStorageBuilder.fillDomainObjectTypesHierarchyMap(typeName);
            }
            return getReturnObject(result);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String[] getDomainObjectTypesHierarchyBeginningFromType(String typeName) {
        readLock.lock();
        try {
            String[] result = this.configStorage.domainObjectTypesHierarchyBeginningFromType.get(typeName);
            if (result == null) {
                configurationStorageBuilder.fillDomainObjectTypesHierarchyMap(typeName);
                result = this.configStorage.domainObjectTypesHierarchyBeginningFromType.get(typeName);
            }
            return getReturnObject(result);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void updateConfig(TopLevelConfig config) {
        writeLock.lock();
        try {
            TopLevelConfig oldConfig = getConfig(config.getClass(), config.getName());
            applicationEventPublisher.publishEvent(new ConfigurationUpdateEvent(this, configStorage, oldConfig, config));
        } finally {
            writeLock.unlock();
        }
    }

    private <T> T getReturnObject(Object source) {
        //return objectCloner.cloneObject(source, tClass);
        // cloning is switched off for performance purpose
        return (T) source;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public List<String> getAllowedToCreateUserGroups(String objectType) {
        List<String> userGroups = new ArrayList<>();

        AccessMatrixConfig accessMatrix = getAccessMatrixByObjectTypeUsingExtension(objectType);

        if (accessMatrix != null && accessMatrix.getCreateConfig() != null && accessMatrix.getCreateConfig().getPermitGroups() != null) {
            for (PermitGroup permitGroup : accessMatrix.getCreateConfig().getPermitGroups()) {
                userGroups.add(permitGroup.getName());
            }
        }
        return userGroups;
    }

    @Override
    public EventLogsConfig getEventLogsConfiguration() {
        GlobalSettingsConfig globalSettings = getGlobalSettings();
        return globalSettings != null ? globalSettings.getEventLogsConfig() : null;
    }

    @Override
    public LogDomainObjectAccessConfig getDomainObjectAccessEventLogsConfiguration(String typeName) {
        readLock.lock();
        try {
            if (this.configStorage.eventLogDomainObjectAccessConfig.containsKey(typeName)) {
                return getReturnObject(this.configStorage.eventLogDomainObjectAccessConfig.get(typeName));
            } else {
                return getReturnObject(this.configStorage.eventLogDomainObjectAccessConfig.get("*"));
            }
        } finally {
            readLock.unlock();
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
            readLock.lock();
            try {
                CaseInsensitiveMap<LocalizableConfig> typeMap =
                        configStorage.localizedConfigMap.get(new Pair<String, Class>(currentLocale, type));
                if (typeMap != null) {
                    T config = (T) typeMap.get(name);
                    return getReturnObject(config);
                }
            } finally {
                readLock.unlock();
            }
        }
        return getConfig(type, name);
    }

    @Override
    public <T> Collection<T> getLocalizedConfigs(Class<T> type, String currentLocale) {
        readLock.lock();
        try {
            CaseInsensitiveMap<LocalizableConfig> typeMap = configStorage.localizedConfigMap.get(
                    new Pair<String, Class>(currentLocale, type));
            if (typeMap == null) {
                return Collections.EMPTY_LIST;
            }
//            //Перекладываем в другой контейнер, для возможности сериализации
            List<T> result = new ArrayList<T>();
            result.addAll((Collection<T>) typeMap.values());
            return getReturnObject(typeMap.values());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public FormConfig getPlainFormConfig(String name) {
        FormConfig formConfig = getConfig(FormConfig.class, name);
        readLock.lock();
        try {
            if (plainFormBuilder.isRaw(formConfig)) {
                FormConfig formConfigFromCash = configStorage.collectedFormConfigMap.get(name);
                if (formConfigFromCash == null) {
                    formConfig = plainFormBuilder.buildPlainForm(formConfig);

                    if (formConfig == null) {
                        formConfig = NullValues.FORM_CONFIG;
                    }

                    configStorage.collectedFormConfigMap.put(formConfig.getName(), formConfig);
                } else {
                    formConfig = formConfigFromCash;
                }
            }

            if (NullValues.isNull(formConfig)) {
                return null;
            }

            return getReturnObject(formConfig);
        } finally {
            readLock.unlock();
        }

    }

    @Override
    public FormConfig getLocalizedPlainFormConfig(String name, String currentLocale) {
        readLock.lock();
        try {
            CaseInsensitiveMap<FormConfig> typeMap = configStorage.localizedCollectedFormConfigMap.get(currentLocale);
            if (typeMap == null) {
                typeMap = new CaseInsensitiveMap<>();
                configStorage.localizedCollectedFormConfigMap.put(currentLocale, typeMap);
            }

            FormConfig formConfig = typeMap.get(name);
            if (formConfig == null) {
                formConfig = getPlainFormConfig(name);
                ObjectCloner cloner = ObjectCloner.getInstance();
                FormConfig clonedConfig = cloner.cloneObject(formConfig, FormConfig.class);
                configurationStorageBuilder.localize(currentLocale, clonedConfig);

                if (clonedConfig == null) {
                    clonedConfig = NullValues.FORM_CONFIG;
                }

                typeMap.put(formConfig.getName(), clonedConfig);
                formConfig = clonedConfig;
            }

            if (NullValues.isNull(formConfig)) {
                return null;
            }

            return formConfig;
        } finally {
            readLock.unlock();
        }
    }

    public List<FormConfig> getParentFormConfigs(FormConfig formConfig) {
        if (formConfig == null) {
            return Collections.EMPTY_LIST;
        }
        readLock.lock();
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
            readLock.unlock();
        }

    }

    private FormConfig getParent(FormConfig formConfig) {
        FormConfig result = null;
        if (formConfig.getExtends() != null) {
            result = getConfig(FormConfig.class, formConfig.getExtends());
        }
        return result;
    }
}
