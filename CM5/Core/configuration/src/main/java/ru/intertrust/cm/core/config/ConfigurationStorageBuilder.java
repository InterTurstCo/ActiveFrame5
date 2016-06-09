package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.Localizable;
import ru.intertrust.cm.core.config.base.LocalizableConfig;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.eventlog.DomainObjectAccessConfig;
import ru.intertrust.cm.core.config.eventlog.EventLogsConfig;
import ru.intertrust.cm.core.config.eventlog.LogDomainObjectAccessConfig;
import ru.intertrust.cm.core.config.form.PlainFormBuilder;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.AnnotationScanCallback;
import ru.intertrust.cm.core.util.AnnotationScanner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.locks.Lock;

import static ru.intertrust.cm.core.config.NullValues.isNull;

public class ConfigurationStorageBuilder {
    private static Logger log = LoggerFactory.getLogger(ConfigurationStorageBuilder.class);

    private static final String ALL_STATUSES_SIGN = "*";
    private final static String GLOBAL_SETTINGS_CLASS_NAME = "ru.intertrust.cm.core.config.GlobalSettingsConfig";

    private ConfigurationExplorer configurationExplorer;
    private ConfigurationStorage configurationStorage;
    private Lock writeLock;

    public ConfigurationStorageBuilder(ConfigurationExplorer configurationExplorer, ConfigurationStorage configurationStorage) {
        this.configurationExplorer = configurationExplorer;
        this.configurationStorage = configurationStorage;
        this.writeLock = configurationExplorer.getReadWriteLock().writeLock();
    }

    public void buildConfigurationStorage() {
        lock();

        try {
            initConfigurationMaps();
        } finally {
            unlock();
        }
    }

    public void fillTopLevelConfigMap(TopLevelConfig config) {
        lock();

        try {
            CaseInsensitiveMap<TopLevelConfig> typeMap = configurationStorage.topLevelConfigMap.get(config.getClass());
            if (typeMap == null) {
                typeMap = new CaseInsensitiveMap<>();
                configurationStorage.topLevelConfigMap.put(config.getClass(), typeMap);
            }
            typeMap.put(config.getName(), config);
        } finally {
            unlock();
        }
    }

    public void fillLocalizedConfigMaps(TopLevelConfig config) {
        lock();

        try {
            if (!(config instanceof LocalizableConfig)) {
                return;
            }

            for (String locale : MessageResourceProvider.getAvailableLocales()) {
                fillLocalizedConfigMap(locale, (LocalizableConfig) config);
            }
        } finally {
            unlock();
        }
    }

    private void fillLocalizedConfigMap(String locale, LocalizableConfig config) {
        lock();

        try {
            Pair<String, Class<?>> key = new Pair<String, Class<?>>(locale, config.getClass());
            CaseInsensitiveMap<LocalizableConfig> typeMap = configurationStorage.localizedConfigMap.get(key);
            if (typeMap == null) {
                typeMap = new CaseInsensitiveMap<>();
                configurationStorage.localizedConfigMap.put(key, typeMap);
            }

            LocalizableConfig clonedConfig = ObjectCloner.getInstance().cloneObject(config, config.getClass());
            localize(locale, clonedConfig);
            typeMap.put(config.getName(), clonedConfig);
        } finally {
            unlock();
        }
    }

    public void localize(final String locale, LocalizableConfig config) {
        lock();

        try {
            AnnotationScanner.scanAnnotation(config, Localizable.class, new AnnotationScanCallback() {
                @Override
                public void onAnnotationFound(Object object, Field field) throws IllegalAccessException {
                    if (field.get(object) != null) {
                        String originalValue = (String) field.get(object);
                        String localizedValue = MessageResourceProvider.getMessage(originalValue, locale);
                        field.set(object, localizedValue);
                    }
                }
            });
        } catch (IllegalAccessException e) {
            throw new ConfigurationException(e);
        } finally {
            unlock();
        }
    }

    public void fillGlobalSettingsCache(TopLevelConfig config) {
        lock();

        try {
            if (GLOBAL_SETTINGS_CLASS_NAME.equalsIgnoreCase(config.getClass().getCanonicalName())) {
                configurationStorage.globalSettings = (GlobalSettingsConfig) config;
            }
        } finally {
            unlock();
        }
    }

    public void fillCollectionColumnConfigMap(CollectionViewConfig collectionViewConfig) {
        lock();

        try {
            if (collectionViewConfig.getCollectionDisplayConfig() != null) {
                for (CollectionColumnConfig columnConfig : collectionViewConfig.getCollectionDisplayConfig().
                        getColumnConfig()) {
                    FieldConfigKey fieldConfigKey =
                            new FieldConfigKey(collectionViewConfig.getName(), columnConfig.getField());
                    configurationStorage.collectionColumnConfigMap.put(fieldConfigKey, columnConfig);
                }
            }
        } finally {
            unlock();
        }
    }

    public void updateCollectionColumnConfigMap(CollectionViewConfig oldConfig, CollectionViewConfig newConfig) {
        lock();

        try {
            if (oldConfig != null && oldConfig.getCollectionDisplayConfig() != null) {
                for (CollectionColumnConfig columnConfig : oldConfig.getCollectionDisplayConfig().getColumnConfig()) {
                    FieldConfigKey fieldConfigKey = new FieldConfigKey(oldConfig.getName(), columnConfig.getField());
                    configurationStorage.collectionColumnConfigMap.remove(fieldConfigKey);
                }
            }

            fillCollectionColumnConfigMap(newConfig);
        } finally {
            unlock();
        }

    }

    public void updateToolbarConfigByPluginMap(ToolBarConfig oldConfig, ToolBarConfig newConfig) {
        lock();

        try {
            if (oldConfig != null) {
                configurationStorage.toolbarConfigByPluginMap.remove(oldConfig.getPlugin());
                for (String locale : MessageResourceProvider.getAvailableLocales()) {
                    CaseInsensitiveMap<ToolBarConfig> toolbarMap =  configurationStorage.localizedToolbarConfigMap.get(locale);
                    if (toolbarMap != null) {
                        toolbarMap.remove(oldConfig.getPlugin());
                    }
                }
            }
            fillToolbarConfigByPluginMap(newConfig);
            for (String locale : MessageResourceProvider.getAvailableLocales()) {
                fillLocalizedToolbarConfigMap(newConfig, locale);
            }
        } finally {
            unlock();
        }
    }

    public void fillToolbarConfigByPluginMap(ToolBarConfig toolBarConfig) {
        lock();

        try {
            if (configurationStorage.toolbarConfigByPluginMap.get(toolBarConfig.getPlugin()) == null) {
                configurationStorage.toolbarConfigByPluginMap.put(toolBarConfig.getPlugin(), toolBarConfig);
            }
        } finally {
            unlock();
        }
    }

    public void fillLocalizedToolbarConfigMap(ToolBarConfig toolBarConfig, String locale) {
        lock();

        try {
            CaseInsensitiveMap<ToolBarConfig> toolbarMap = configurationStorage.localizedToolbarConfigMap.get(locale);
            if (toolbarMap == null) {
                toolbarMap = new CaseInsensitiveMap<>();
                configurationStorage.localizedToolbarConfigMap.put(locale, toolbarMap);
            }
            if (toolbarMap.get(toolBarConfig.getPlugin()) == null) {
                ToolBarConfig clonedConfig = ObjectCloner.getInstance().cloneObject(toolBarConfig, toolBarConfig.getClass());
                localize(locale, clonedConfig);
                toolbarMap.put(clonedConfig.getPlugin(), clonedConfig);
            }
        } finally {
            unlock();
        }
    }

    public List<DynamicGroupConfig> fillDynamicGroupConfigContextMap(String domainObjectType) {
        lock();

        try {
            List<DynamicGroupConfig> dynamicGroups = new ArrayList<>();

            Collection<DynamicGroupConfig> dynamicGroupConfigs = configurationExplorer.getConfigs(DynamicGroupConfig.class);
            for (DynamicGroupConfig dynamicGroup : dynamicGroupConfigs) {
                if (dynamicGroup.getContext() != null && dynamicGroup.getContext().getDomainObject() != null) {
                    String objectType = dynamicGroup.getContext().getDomainObject().getType();

                    if (objectType.equals(domainObjectType)) {
                        dynamicGroups.add(dynamicGroup);
                    }
                }
            }

            configurationStorage.dynamicGroupConfigByContextMap.put(domainObjectType, Collections.unmodifiableList(dynamicGroups));
            return dynamicGroups;
        } finally {
            unlock();
        }
    }

    public List<DynamicGroupConfig> fillDynamicGroupConfigsByTrackDOMap(String trackDOTypeName, String status) {
        lock();

        try {
            List<DynamicGroupConfig> resultList = new ArrayList<>();
            Collection<DynamicGroupConfig> dynamicGroups = configurationExplorer.getConfigs(DynamicGroupConfig.class);

            for (DynamicGroupConfig dynamicGroup : dynamicGroups) {
                if (dynamicGroup.getMembers() != null && dynamicGroup.getMembers().getTrackDomainObjects() != null) {
                    List<DynamicGroupTrackDomainObjectsConfig> trackDomainObjectConfigs =
                            dynamicGroup.getMembers().getTrackDomainObjects();
                    for (DynamicGroupTrackDomainObjectsConfig trackDomainObjectConfig : trackDomainObjectConfigs) {
                        String configuredStatus = trackDomainObjectConfig.getStatus();
                        String configuredType = trackDomainObjectConfig.getType();
                        if (trackDOTypeName.equalsIgnoreCase(configuredType)) {
                            if (configuredStatus == null || configuredStatus.equals(status)) {
                                resultList.add(dynamicGroup);
                            }
                        }
                    }
                }
            }

            FieldConfigKey fieldConfigKey = new FieldConfigKey(trackDOTypeName, status);
            configurationStorage.dynamicGroupConfigsByTrackDOMap.put(fieldConfigKey, Collections.unmodifiableList(resultList));

            return resultList;
        } finally {
            unlock();
        }
    }

    public AccessMatrixStatusConfig fillAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status) {
        lock();

        try {
            if (status == null) {
                status = "*";
            }

            AccessMatrixStatusConfig result = null;

            //Получение конфигурации матрицы
            AccessMatrixConfig accessMatrixConfig = configurationExplorer.getConfig(AccessMatrixConfig.class, domainObjectType);
            if (accessMatrixConfig == null) {
                //Если матрица не найдена то ищем матрицу для родительского типа
                DomainObjectTypeConfig doConfig = configurationExplorer.getDomainObjectTypeConfig(domainObjectType);
                if (doConfig.getExtendsAttribute() != null) {
                    result = fillAccessMatrixByObjectTypeAndStatus(doConfig.getExtendsAttribute(), status);
                }
            } else if (accessMatrixConfig.getStatus() != null) {
                //Получаем все статусы
                for (AccessMatrixStatusConfig accessStatusConfig : accessMatrixConfig.getStatus()) {
                    //Если статус в конфигурации звезда то не проверяем статусы на соответствие, а возвращаем текущий
                    if (accessStatusConfig.getName().equals("*")) {
                        result = accessStatusConfig;
                        break;
                    } else if (status != null && status.equalsIgnoreCase(accessStatusConfig.getName())) {
                        result = accessStatusConfig;
                        break;
                    }
                }
            }

            if (result == null) {
                result = NullValues.ACCESS_MATRIX_STATUS_CONFIG;
            }

            configurationStorage.accessMatrixByObjectTypeAndStatusMap.put(new FieldConfigKey(domainObjectType, status), result);
            return result;
        } finally {
            unlock();
        }
    }

    public String fillMatrixReferenceTypeNameMap(String childTypeName) {
        lock();

        try {
            //Получаем матрицу и смотрим атрибут matrix_reference_field
            AccessMatrixConfig matrixConfig = null;
            DomainObjectTypeConfig childDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, childTypeName);
            if (childDomainObjectTypeConfig == null) {
                return null; // todo: throw exception
            }

            String result = null;

            //Ищим матрицу для типа с учетом иерархии типов
            while ((matrixConfig = configurationExplorer.getAccessMatrixByObjectType(childDomainObjectTypeConfig.getName())) == null
                    && childDomainObjectTypeConfig.getExtendsAttribute() != null) {
                childDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, childDomainObjectTypeConfig.getExtendsAttribute());
            }

            if (matrixConfig != null && matrixConfig.getMatrixReference() != null) {
                //Получаем имя типа на которого ссылается martix-reference-field
                String parentTypeName = getParentTypeNameFromMatrixReference(matrixConfig.getMatrixReference(), childDomainObjectTypeConfig);
                //Вызываем рекурсивно метод для родительского типа, на случай если в родительской матрице так же заполнено поле martix-reference-field
                result = configurationExplorer.getMatrixReferenceTypeName(parentTypeName);
                //В случае если у родителя не заполнен атрибут martix-reference-field то возвращаем имя родителя
                if (result == null) {
                    result = parentTypeName;
                }
            }

            // TODO Discuss - need to delete
            //В случае если не найдена матрица, то возможно это абстракный тип и надо поискать матрицы для потомков.
            if (matrixConfig == null) {
                //Получаем все потомки
                List<String> childTypes = getChildTypes(childTypeName);
                //Ищим первую матрицу у потомков и проверяем ее тип
                for (String childType : childTypes) {
                    matrixConfig = configurationExplorer.getAccessMatrixByObjectType(childType);
                    if (matrixConfig != null) {
                        //у потомка заимствованные права, значит и у родителя заимствованные права, получаем тип откуда заимсвует права потомок
                        if (matrixConfig.getMatrixReference() != null) {
                            result = fillMatrixReferenceTypeNameMap(childType);
                        }else{
                            result = childType;
                        }
                        //Выходим из цикла при первой же обнаруженной матрице
                        break;
                    }
                }
            }

            if (result == null) {
                result = NullValues.STRING;
            }

            configurationStorage.matrixReferenceTypeNameMap.put(childTypeName, result);
            return result;
        } finally {
            unlock();
        }
    }

    public Set<String> fillTypesDelegatingAccessCheckTo(String typeName) {
        lock();

        try {
            DomainObjectTypeConfig typeConfig = configurationExplorer.getDomainObjectTypeConfig(typeName);
            if (typeConfig == null) {
                throw new IllegalArgumentException("Domain Object Type " + typeName + " doesn't exist");
            }

            TypesDelegatingAccessCheckToBuilderData builderData = new TypesDelegatingAccessCheckToBuilderData();

            fillTypesDelegatingAccessCheckTo(typeConfig.getName(), builderData);

            Set<String> result = new HashSet<>();
            Set<String> resultInLowerCase = new HashSet<>();

            String delegatedTo = configurationExplorer.getMatrixReferenceTypeName(typeConfig.getName());
            if (delegatedTo == null) {
                result.add(typeConfig.getName());
                resultInLowerCase.add(typeConfig.getName().toLowerCase());
            }

            result.addAll(builderData.getHierarchicalDelegates());
            resultInLowerCase.addAll(builderData.getHierarchicalDelegatesInLowerCase());

            result.addAll(builderData.getMatrixDelegates());
            resultInLowerCase.addAll(builderData.getMatrixDelegatesInLowerCase());

            configurationStorage.typesDelegatingAccessCheckTo.put(typeConfig.getName(), result);
            configurationStorage.typesDelegatingAccessCheckToInLowerCase.put(typeConfig.getName(), resultInLowerCase);

            return result;
        } finally {
            unlock();
        }
    }

    public String[] fillDomainObjectTypesHierarchyMap(String typeName) {
        lock();

        try {
            List<String> typesHierarchy = new ArrayList<>();
            buildDomainObjectTypesHierarchy(typesHierarchy, typeName);

            List<String> typesInAscendingOrder = new ArrayList<>(typesHierarchy.size() + 1);
            typesInAscendingOrder.add(typeName);
            typesInAscendingOrder.addAll(typesHierarchy);
            configurationStorage.domainObjectTypesHierarchyBeginningFromType.put(typeName, typesInAscendingOrder.toArray(new String[typesInAscendingOrder.size()]));

            Collections.reverse(typesHierarchy);
            String[] types = typesHierarchy.toArray(new String[typesHierarchy.size()]);
            configurationStorage.domainObjectTypesHierarchy.put(typeName, types);
            return types;
        } finally {
            unlock();
        }
    }

    public void updateDomainObjectFieldConfig(DomainObjectTypeConfig oldType, DomainObjectTypeConfig newType) {
        lock();

        try {
            if (oldType != null) {
                removeDomainObjectFieldConfigsFromMap(oldType);
            }
            fillDomainObjectFieldConfig(newType);
        } finally {
            unlock();
        }
    }

    public void updateConfigurationMapOfChildDomainObjectType(DomainObjectTypeConfig type) {
        lock();

        try {
            String typeName = type.getName();

            configurationStorage.directChildDomainObjectTypesMap.remove(typeName);
            configurationStorage.indirectChildDomainObjectTypesMap.remove(typeName);

            fillConfigurationMapOfChildDomainObjectType(type);
        } finally {
            unlock();
        }
    }

    public void updateConfigurationMapsOfAttachmentDomainObjectType(DomainObjectTypeConfig oldConfig,
                                                                    DomainObjectTypeConfig newConfig) {
        lock();

        try {
            if (oldConfig != null && oldConfig.getAttachmentTypesConfig() != null) {
                AttachmentPrototypeHelper attachmentPrototypeHelper = new AttachmentPrototypeHelper();
                for (AttachmentTypeConfig attachmentTypeConfig :
                        oldConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs()) {

                    DomainObjectTypeConfig attachmentDomainObjectTypeConfig =
                            attachmentPrototypeHelper.makeAttachmentConfig(attachmentTypeConfig.getTemplate(), attachmentTypeConfig.getName(),
                                    oldConfig.getName());

                    removeTopLevelConfigFromMap(attachmentDomainObjectTypeConfig);
                    removeDomainObjectFieldConfigsFromMap(attachmentDomainObjectTypeConfig);
                    configurationStorage.attachmentDomainObjectTypes.remove(attachmentDomainObjectTypeConfig.getName());
                }
            }

            fillConfigurationMapsOfAttachmentDomainObjectType(newConfig);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        } finally {
            unlock();
        }
    }

    public void updateAuditLogConfigs(DomainObjectTypeConfig oldConfig, DomainObjectTypeConfig newConfig) {
        lock();

        try {
            if (oldConfig != null && oldConfig.isTemplate()) {
                return;
            }

            if (oldConfig != null) {
                DomainObjectTypeConfig oldAuditLogConfig = createAuditLogConfig(oldConfig);
                removeTopLevelConfigFromMap(oldAuditLogConfig);
                removeDomainObjectFieldConfigsFromMap(oldAuditLogConfig);
                configurationStorage.auditLogTypes.remove(oldAuditLogConfig.getName());
            }
            fillAuditLogConfigMap(newConfig);
        } finally {
            unlock();
        }
    }

    public AccessMatrixConfig fillAccessMatrixByObjectTypeUsingExtension(String domainObjectType) {
        lock();

        try {
            if (configurationExplorer.isAuditLogType(domainObjectType)) {
                domainObjectType = getParentTypeOfAuditLog(domainObjectType);
            }

            AccessMatrixConfig accessMatrixConfig = configurationExplorer.getConfig(AccessMatrixConfig.class, domainObjectType);

            if (accessMatrixConfig == null) {
                DomainObjectTypeConfig domainObjectTypeConfig =
                        configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectType);
                if (domainObjectTypeConfig != null && domainObjectTypeConfig.getExtendsAttribute() != null) {
                    String parentDOType = domainObjectTypeConfig.getExtendsAttribute();
                    accessMatrixConfig = fillAccessMatrixByObjectTypeUsingExtension(parentDOType);
                }
            }

            if (accessMatrixConfig == null) {
                accessMatrixConfig = NullValues.ACCESS_MATRIX_CONFIG;
            }

            configurationStorage.accessMatrixByObjectTypeUsingExtensionMap.put(domainObjectType, accessMatrixConfig);
            return accessMatrixConfig;
        } finally {
            unlock();
        }
    }

    public Set<ReferenceFieldConfig> fillReferenceFieldsMap(String domainObjectTypeConfigName) {
        lock();

        try {
            String[] hierarchy = configurationExplorer.getDomainObjectTypesHierarchy(domainObjectTypeConfigName);

            List<String> allTypeNames = hierarchy == null ? new ArrayList<String>(1) : new ArrayList<String>(hierarchy.length + 1);
            allTypeNames.add(domainObjectTypeConfigName);
            if (hierarchy != null) {
                allTypeNames.addAll(Arrays.asList(hierarchy));
            }

            Set<ReferenceFieldConfig> referenceFieldConfigs = new HashSet<>();
            Set<ReferenceFieldConfig> immutableReferenceFieldConfigs = new HashSet<>();
            for (String typeName : allTypeNames) {
                DomainObjectTypeConfig config = configurationExplorer.getDomainObjectTypeConfig(typeName);
                for (FieldConfig fieldConfig : config.getFieldConfigs()) {
                    if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                        ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
                        referenceFieldConfigs.add(referenceFieldConfig);
                        if (fieldConfig.isImmutable()) {
                            immutableReferenceFieldConfigs.add(referenceFieldConfig);
                        }
                    }
                }
            }

            configurationStorage.referenceFieldsMap.put(domainObjectTypeConfigName, Collections.unmodifiableSet(referenceFieldConfigs));
            configurationStorage.immutableReferenceFieldsMap.put(domainObjectTypeConfigName, Collections.unmodifiableSet(immutableReferenceFieldConfigs));
            return referenceFieldConfigs;
        } finally {
            unlock();
        }
    }

    public String fillTypeInHierarchyHavingField(String doType, String fieldName) {
        lock();

        try {
            FieldConfigKey fieldConfigKey = new FieldConfigKey(doType, fieldName);
            FieldConfig fieldConfig = configurationExplorer.getFieldConfig(doType, fieldName, false);

            if (fieldConfig != null) {
                configurationStorage.typeInHierarchyHavingFieldMap.put(fieldConfigKey, doType);
                return doType;
            }

            DomainObjectTypeConfig doTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, doType);
            if (doTypeConfig == null || doTypeConfig.getExtendsAttribute() == null) {
                throw new ConfigurationException("Field '" + fieldName +
                        "' is not found in hierarchy of domain object type '" + doType + "'");
            }

            String result = configurationExplorer.getFromHierarchyDomainObjectTypeHavingField(doTypeConfig.getExtendsAttribute(), fieldName);
            if (isNull(result)) {
                configurationStorage.typeInHierarchyHavingFieldMap.put(fieldConfigKey, NullValues.STRING);
                return null;
            } else {
                configurationStorage.typeInHierarchyHavingFieldMap.put(fieldConfigKey, result);
                return result;
            }
        } finally {
            unlock();
        }
    }

    public FieldConfig fillFieldConfigMap(String domainObjectConfigName, String fieldConfigName, boolean returnInheritedConfig) {
        lock();

        try {
            FieldConfigKey fieldConfigKey = new FieldConfigKey(domainObjectConfigName, fieldConfigName, returnInheritedConfig);

            if (!returnInheritedConfig) {
                configurationStorage.fieldConfigMap.put(fieldConfigKey, NullValues.FIELD_CONFIG);
                return null;
            }

            FieldConfigKey fieldConfigKeyWithoutInheritance =
                    new FieldConfigKey(domainObjectConfigName, fieldConfigName, false);
            FieldConfig result = configurationStorage.fieldConfigMap.get(fieldConfigKeyWithoutInheritance);

            if (!isNull(result)) {
                configurationStorage.fieldConfigMap.put(fieldConfigKey, result);
                return result;
            }

            DomainObjectTypeConfig domainObjectTypeConfig =
                    configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectConfigName);

            if (domainObjectTypeConfig == null || domainObjectTypeConfig.getExtendsAttribute() == null) {
                configurationStorage.fieldConfigMap.put(fieldConfigKey, NullValues.FIELD_CONFIG);
                return null;
            }

            result = configurationExplorer.getFieldConfig(domainObjectTypeConfig.getExtendsAttribute(), fieldConfigName);
            if (isNull(result)) {
                configurationStorage.fieldConfigMap.put(fieldConfigKey, NullValues.FIELD_CONFIG);
                return null;
            } else {
                configurationStorage.fieldConfigMap.put(fieldConfigKey, result);
                return result;
            }
        } finally {
            unlock();
        }
    }

    public List<String> fillAllowedToCreateUserGroups(String objectType) {
        lock();

        try {
            List<String> userGroups = new ArrayList<>();
            AccessMatrixConfig accessMatrix = configurationExplorer.getAccessMatrixByObjectTypeUsingExtension(objectType);

            if (accessMatrix != null && accessMatrix.getCreateConfig() != null && accessMatrix.getCreateConfig().getPermitGroups() != null) {
                for (PermitGroup permitGroup : accessMatrix.getCreateConfig().getPermitGroups()) {
                    userGroups.add(permitGroup.getName());
                }
            }

            userGroups = Collections.unmodifiableList(userGroups);
            configurationStorage.allowedToCreateUserGroupsMap.put(objectType, userGroups);

            return userGroups;
        } finally {
            unlock();
        }
    }

    public FormConfig fillPlainFormConfigMap(FormConfig formConfig, PlainFormBuilder plainFormBuilder) {
        lock();

        try {
            formConfig = plainFormBuilder.buildPlainForm(formConfig);

            if (formConfig == null) {
                formConfig = NullValues.FORM_CONFIG;
            }

            configurationStorage.collectedFormConfigMap.put(formConfig.getName(), formConfig);
            return formConfig;
        } finally {
            unlock();
        }
    }

    public FormConfig fillLocalizedPlainFormConfigMap(String name, String currentLocale) {
        lock();

        try {
            CaseInsensitiveMap<FormConfig> typeMap = configurationStorage.localizedCollectedFormConfigMap.get(currentLocale);
            if (typeMap == null) {
                typeMap = new CaseInsensitiveMap<>();
                configurationStorage.localizedCollectedFormConfigMap.put(currentLocale, typeMap);
            }

            FormConfig formConfig = configurationExplorer.getPlainFormConfig(name);
            ObjectCloner cloner = ObjectCloner.getInstance();
            FormConfig clonedConfig = cloner.cloneObject(formConfig, FormConfig.class);
            localize(currentLocale, clonedConfig);

            if (clonedConfig == null) {
                clonedConfig = NullValues.FORM_CONFIG;
            }

            typeMap.put(formConfig.getName(), clonedConfig);
            return clonedConfig;
        } finally {
            unlock();
        }
    }

    private void fillTypesDelegatingAccessCheckTo(String typeName, TypesDelegatingAccessCheckToBuilderData builderData) {
        lock();

        try {
            Set<String> newDelegatingTypes = new HashSet<>();

            Collection<DomainObjectTypeConfig> childTypes = configurationExplorer.findChildDomainObjectTypes(typeName, true);
            for (DomainObjectTypeConfig childType : childTypes) {
                if (builderData.getHierarchicalDelegates().contains(childType.getName())) {
                    log.warn("Cycle detected in Domain Object Type hierarchy for " + childType.getName());
                } else {
                    builderData.getHierarchicalDelegates().add(childType.getName());
                    builderData.getHierarchicalDelegatesInLowerCase().add(childType.getName().toLowerCase());
                    newDelegatingTypes.add(childType.getName());
                }
            }

            Collection<AccessMatrixConfig> matrices = configurationExplorer.getConfigs(AccessMatrixConfig.class);
            for (AccessMatrixConfig matrixConfig : matrices) {
                if (matrixConfig.getMatrixReference() == null || !matrixConfig.getMatrixReference().equalsIgnoreCase(typeName)) {
                    continue;
                }

                if (builderData.getMatrixDelegates().contains(matrixConfig.getType())) {
                    log.warn("Cycle detected in access rights delegation for " + matrixConfig.getType());
                } else {
                    builderData.getMatrixDelegates().add(matrixConfig.getType());
                    builderData.getMatrixDelegatesInLowerCase().add(matrixConfig.getType().toLowerCase());
                    newDelegatingTypes.add(matrixConfig.getType());
                }
            }

            for (String referencingType : newDelegatingTypes) {
                fillTypesDelegatingAccessCheckTo(referencingType, builderData);
            }
        } finally {
            unlock();
        }
    }

    private String getParentTypeOfAuditLog(String domainObjectType) {
        domainObjectType = domainObjectType.replace(Configuration.AUDIT_LOG_SUFFIX, "");
        return domainObjectType;
    }

    /**
     * Получение всех дочерних типов с учетом иерархии наследования
     * @param typeName
     * @return
     */
    private List<String> getChildTypes(String typeName) {
        List<String> result = new ArrayList<String>();
        Collection<DomainObjectTypeConfig> allTypes = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        for (DomainObjectTypeConfig domainObjectTypeConfig : allTypes) {
            if (domainObjectTypeConfig.getExtendsAttribute() != null && domainObjectTypeConfig.getExtendsAttribute().equalsIgnoreCase(typeName)) {
                result.add(domainObjectTypeConfig.getName());
                result.addAll(getChildTypes(domainObjectTypeConfig.getName()));
            }
        }
        return result;
    }

    private void removeTopLevelConfigFromMap(TopLevelConfig config) {
        CaseInsensitiveMap<TopLevelConfig> typeMap = configurationStorage.topLevelConfigMap.get(config.getClass());
        if (typeMap != null) {
            typeMap.remove(config.getName());
        }
    }

    private void removeDomainObjectFieldConfigsFromMap(DomainObjectTypeConfig oldType) {
        if (oldType == null) {
            return;
        }

        List<FieldConfig> allFieldsConfig = DomainObjectTypeUtility.getAllFieldConfigs(oldType.getDomainObjectFieldsConfig(), configurationExplorer);

        for (FieldConfig fieldConfig : allFieldsConfig) {
            FieldConfigKey fieldConfigKey = new FieldConfigKey(oldType.getName(), fieldConfig.getName());
            configurationStorage.fieldConfigMap.remove(fieldConfigKey);
        }
        for (FieldConfig fieldConfig : oldType.getSystemFieldConfigs()) {
            FieldConfigKey fieldConfigKey = new FieldConfigKey(oldType.getName(), fieldConfig.getName());
            configurationStorage.fieldConfigMap.remove(fieldConfigKey);
        }
    }

    private void fillDomainObjectFieldConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        List<FieldConfig> allFieldsConfig =
                DomainObjectTypeUtility.getAllFieldConfigs(domainObjectTypeConfig.getDomainObjectFieldsConfig(), configurationExplorer);
        domainObjectTypeConfig.getDomainObjectFieldsConfig().setFieldConfigs(allFieldsConfig);

        fillFieldsConfigMap(domainObjectTypeConfig);
    }

    private void fillConfigurationMapOfChildDomainObjectType(DomainObjectTypeConfig type) {
        ArrayList<DomainObjectTypeConfig> directChildTypes = new ArrayList<>();
        ArrayList<DomainObjectTypeConfig> indirectChildTypes = new ArrayList<>();
        String typeName = type.getName();

        initConfigurationMapOfChildDomainObjectTypes(typeName, directChildTypes, indirectChildTypes, true);
        configurationStorage.directChildDomainObjectTypesMap.put(typeName, Collections.unmodifiableList(directChildTypes));
        configurationStorage.indirectChildDomainObjectTypesMap.put(typeName, Collections.unmodifiableList(indirectChildTypes));
    }

    /**
     * Получение типа, на который ссылается атрибут известного типа
     * @param matrixReferenceFieldName
     * @param domainObjectTypeConfig
     * @return
     */
    private String getParentTypeNameFromMatrixReference(String matrixReferenceFieldName,
                                                        DomainObjectTypeConfig domainObjectTypeConfig) {

        String result = null;
        if (matrixReferenceFieldName.indexOf(".") > 0) {
            // TODO здесь надо добавить обработку backlink
            throw new UnsupportedOperationException("Not implemented access referencing using backlink.");
        } else {
            ReferenceFieldConfig fieldConfig =
                    (ReferenceFieldConfig) configurationExplorer.getFieldConfig(domainObjectTypeConfig.getName(), matrixReferenceFieldName);
            if (fieldConfig != null) {  // todo: throw exception if null
                result = fieldConfig.getType();
            }
        }
        return result;
    }

    private void initConfigurationMaps() {
        if (configurationStorage.configuration == null) {
            throw new FatalException("Failed to initialize ConfigurationExplorerImpl because " +
                    "Configuration is null");
        }

        for (TopLevelConfig config : configurationStorage.configuration.getConfigurationList()) {
            fillGlobalSettingsCache(config);
            fillTopLevelConfigMap(config);

            if (CollectionViewConfig.class.equals(config.getClass())) {
                CollectionViewConfig collectionViewConfig = (CollectionViewConfig) config;
                fillCollectionColumnConfigMap(collectionViewConfig);
            } else if (ToolBarConfig.class.equals(config.getClass())) {
                ToolBarConfig toolBarConfig = (ToolBarConfig) config;
                fillToolbarConfigByPluginMap(toolBarConfig);
                for (String locale : MessageResourceProvider.getAvailableLocales()) {
                    fillLocalizedToolbarConfigMap(toolBarConfig, locale);
                }
            }

            if (config instanceof LocalizableConfig) {
                for (String locale : MessageResourceProvider.getAvailableLocales()) {
                    fillLocalizedConfigMap(locale, (LocalizableConfig)config);
                }
            }
        }

        Collection<DomainObjectTypeConfig> domainObjectTypeConfigs = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        for (DomainObjectTypeConfig domainObjectTypeConfig : domainObjectTypeConfigs) {

            fillDomainObjectFieldConfig(domainObjectTypeConfig);
            fillConfigurationMapsOfAttachmentDomainObjectType(domainObjectTypeConfig);
            fillConfigurationMapOfChildDomainObjectType(domainObjectTypeConfig);
            fillAuditLogConfigMap(domainObjectTypeConfig);

            if (domainObjectTypeConfig.getExtendsAttribute() == null) {

            }
        }

        //Заполнение таблицы read-evrybody. Вынесено сюда, потому что не для всех типов существует матрица прав и важно чтобы было заполнена TopLevelConfigMap
        fillReadPermittedToEverybodyMap();

        fillEventLogDomainObjectAccessConfig();
    }

    private void initConfigurationMapOfChildDomainObjectTypes(String typeName, ArrayList<DomainObjectTypeConfig> directChildTypes,
                                                              ArrayList<DomainObjectTypeConfig> indirectChildTypes, boolean fillDirect) {
        Collection<DomainObjectTypeConfig> allTypes = configurationExplorer.getConfigs(DomainObjectTypeConfig.class);
        for (DomainObjectTypeConfig type : allTypes) {
            if (typeName.equals(type.getExtendsAttribute())) {
                if (indirectChildTypes.contains(type)) {
                    throw new ConfigurationException("Loop in the hierarchy, typeName: " + typeName);
                }

                if (fillDirect) {
                    directChildTypes.add(type);
                }
                indirectChildTypes.add(type);
                initConfigurationMapOfChildDomainObjectTypes(type.getName(), directChildTypes, indirectChildTypes, false);
            }
        }
    }

    private void fillSystemFields(DomainObjectTypeConfig domainObjectTypeConfig) {
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getSystemFieldConfigs()) {
            FieldConfigKey fieldConfigKey =
                    new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
            if (GenericDomainObject.STATUS_DO.equals(domainObjectTypeConfig.getName())
                    && GenericDomainObject.STATUS_FIELD_NAME.equals(fieldConfig.getName())) {
                continue;
            }
            configurationStorage.fieldConfigMap.put(fieldConfigKey, fieldConfig);
        }
    }

    private void fillFieldsConfigMap(DomainObjectTypeConfig domainObjectTypeConfig) {
        List<FieldConfig> allFieldsConfig = DomainObjectTypeUtility.getAllFieldConfigs(domainObjectTypeConfig.getDomainObjectFieldsConfig(), configurationExplorer);
        LinkedHashMap<String, FieldConfig> mutableFieldsConfig = new LinkedHashMap<>(allFieldsConfig.size());
        for (FieldConfig fieldConfig : allFieldsConfig) {
            FieldConfigKey fieldConfigKey =
                    new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
            configurationStorage.fieldConfigMap.put(fieldConfigKey, fieldConfig);
            if (!fieldConfig.isImmutable()) {
                mutableFieldsConfig.put(fieldConfig.getName().toLowerCase(), fieldConfig);
            }
        }
        configurationStorage.mutableFieldsNoInheritanceMap.put(domainObjectTypeConfig.getName(), new ArrayList<>(mutableFieldsConfig.values()));
        fillSystemFields(domainObjectTypeConfig);
    }

    private void fillAuditLogConfigMap(DomainObjectTypeConfig domainObjectTypeConfig) {
        if (domainObjectTypeConfig == null || domainObjectTypeConfig.isTemplate()) {
            return;
        }

        DomainObjectTypeConfig auditLogDomainObjectConfig = createAuditLogConfig(domainObjectTypeConfig);

        fillTopLevelConfigMap(auditLogDomainObjectConfig);
        fillFieldsConfigMap(auditLogDomainObjectConfig);
        configurationStorage.auditLogTypes.put(auditLogDomainObjectConfig.getName(), auditLogDomainObjectConfig.getName());

    }

    private DomainObjectTypeConfig createAuditLogConfig(DomainObjectTypeConfig domainObjectTypeConfig) {
        DomainObjectTypeConfig auditLogDomainObjectConfig = new DomainObjectTypeConfig();

        auditLogDomainObjectConfig.setTemplate(false);
        String auditLogTableName = getALTableName(domainObjectTypeConfig.getName());
        auditLogDomainObjectConfig.setName(auditLogTableName);

        if (domainObjectTypeConfig.getExtendsAttribute() == null) {
            FieldConfig operationField = new LongFieldConfig();
            operationField.setName(Configuration.OPERATION_COLUMN);
            auditLogDomainObjectConfig.getFieldConfigs().add(operationField);

            ReferenceFieldConfig domainObjectRefenceField = new ReferenceFieldConfig();
            domainObjectRefenceField.setName(Configuration.DOMAIN_OBJECT_ID_COLUMN);
            domainObjectRefenceField.setType(ConfigurationExplorer.REFERENCE_TYPE_ANY);
            auditLogDomainObjectConfig.getFieldConfigs().add(domainObjectRefenceField);

            StringFieldConfig componentField = new StringFieldConfig();
            componentField.setName(Configuration.COMPONENT_COLUMN);
            componentField.setLength(512);
            auditLogDomainObjectConfig.getFieldConfigs().add(componentField);

            StringFieldConfig ipAddressField = new StringFieldConfig();
            ipAddressField.setName(Configuration.IP_ADDRESS_COLUMN);
            ipAddressField.setLength(16);
            auditLogDomainObjectConfig.getFieldConfigs().add(ipAddressField);

            StringFieldConfig infoField = new StringFieldConfig();
            infoField.setName(Configuration.INFO_COLUMN);
            infoField.setLength(512);
            auditLogDomainObjectConfig.getFieldConfigs().add(infoField);
        } else {
            auditLogDomainObjectConfig.setExtendsAttribute(getALTableName(domainObjectTypeConfig.getExtendsAttribute()));
        }

        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            final FieldConfig clonedConfig = ObjectCloner.getInstance().cloneObject(fieldConfig, fieldConfig.getClass());
            clonedConfig.setNotNull(false);
            auditLogDomainObjectConfig.getFieldConfigs().add(clonedConfig);

        }

        return auditLogDomainObjectConfig;
    }

    private static String getALTableName(String name) {
        return name + Configuration.AUDIT_LOG_SUFFIX;
    }

    private void fillConfigurationMapsOfAttachmentDomainObjectType(DomainObjectTypeConfig domainObjectTypeConfig) {
        if (domainObjectTypeConfig == null || domainObjectTypeConfig.getAttachmentTypesConfig() == null) {
            return;
        }
        try {
            AttachmentPrototypeHelper attachmentPrototypeHelper = new AttachmentPrototypeHelper();
            for (AttachmentTypeConfig attachmentTypeConfig :
                    domainObjectTypeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs()) {
                DomainObjectTypeConfig attachmentDomainObjectTypeConfig =
                        attachmentPrototypeHelper.makeAttachmentConfig(attachmentTypeConfig.getTemplate(), attachmentTypeConfig.getName(),
                                domainObjectTypeConfig.getName());
                fillTopLevelConfigMap(attachmentDomainObjectTypeConfig);
                fillFieldsConfigMap(attachmentDomainObjectTypeConfig);
                configurationStorage.attachmentDomainObjectTypes.put(attachmentDomainObjectTypeConfig.getName(),
                        attachmentDomainObjectTypeConfig.getName());
                fillAuditLogConfigMap(attachmentDomainObjectTypeConfig);

            }
        } catch (IOException e) {
            throw new ConfigurationException(e);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }

    }

    private void buildDomainObjectTypesHierarchy(List<String> typesHierarchy, String typeName) {
        DomainObjectTypeConfig domainObjectTypeConfig = configurationExplorer.getDomainObjectTypeConfig(typeName);
        if (domainObjectTypeConfig != null) {
            String parentType = domainObjectTypeConfig.getExtendsAttribute();
            if (parentType != null && parentType.trim().length() > 0) {
                if (typesHierarchy.contains(parentType)) {
                    throw new ConfigurationException("Loop in the hierarchy, typeName: " + typeName);
                }
                typesHierarchy.add(parentType);
                buildDomainObjectTypesHierarchy(typesHierarchy, parentType);
            }
        }
    }

    private void fillReadPermittedToEverybodyMap() {
        for (DomainObjectTypeConfig config : configurationExplorer.getConfigs(DomainObjectTypeConfig.class)) {
            String domainObjectType = config.getName();
            Boolean readEverybody = isReadEverybodyForType(domainObjectType, new HashSet<String>());

            if (readEverybody == null) {
                readEverybody = false;
            }
            configurationStorage.readPermittedToEverybodyMap.put(config.getName(), readEverybody);
        }
    }

    /**
     * Получение флага read-everybody для типа с учетом иерархии наследования типов и иерархии наследования матриц (связанных через атрибут martix-reference-field)
     * @param domainObjectType
     * @return
     */
    private Boolean isReadEverybodyForType(String domainObjectType, Set<String> visitedTypes) {
        Boolean result = null;

        visitedTypes.add(domainObjectType);

        AccessMatrixConfig accessMatrixConfig =
                configurationExplorer.getAccessMatrixByObjectType(domainObjectType);

        if (accessMatrixConfig != null && accessMatrixConfig.isReadEverybody() != null) {
            result = accessMatrixConfig.isReadEverybody();
        } else {
            DomainObjectTypeConfig domainObjectTypeConfig =
                    configurationExplorer.getConfig(DomainObjectTypeConfig.class, domainObjectType);
            if (domainObjectTypeConfig == null) {
                log.warn("No type defined in access matrix found: " + domainObjectType);
                return false;
            }
            // проход по иерархии родительских типов для доменного объекта. Проверка флага read-everybody в матрицах доступа для родительских типов.
            if (domainObjectTypeConfig.getExtendsAttribute() != null &&
                    !visitedTypes.contains(domainObjectTypeConfig.getExtendsAttribute())) {
                String parentDOType = domainObjectTypeConfig.getExtendsAttribute();
                result = isReadEverybodyForType(parentDOType, visitedTypes);
            } else {
                // domainObjectType является ДО верхнего уровня, и флаг read-everybody не определен.
                // Получаем все дочерние типы и смотрим флаг у них. Возвращаем true если найден хотя бы один тип с
                // флагом read-evrybody
                // Валидатор конфигурации должен обеспечить чтобы если встречается хотя бы один тип с read-everybody то
                // должна отсутствовать матрица у типов в данной иерархии с read-everybody = false
                List<String> childTypes = getChildTypes(domainObjectType);
                for (String childType : childTypes) {
                    AccessMatrixConfig childMatrixConfig = configurationExplorer.getAccessMatrixByObjectType(childType);
                    if (childMatrixConfig != null) {
                        if (childMatrixConfig.isReadEverybody() != null) {
                            result = childMatrixConfig.isReadEverybody();
                            break;
                        } else if (childMatrixConfig.getMatrixReference() != null) {
                            DomainObjectTypeConfig childDomainObjectTypeConfig =
                                    configurationExplorer.getDomainObjectTypeConfig(childType);
                            String referencedTypeName =
                                    getParentTypeNameFromMatrixReference(childMatrixConfig.getMatrixReference(),
                                            childDomainObjectTypeConfig);
                            if (!visitedTypes.contains(referencedTypeName)) {
                                result = isReadEverybodyForType(referencedTypeName, visitedTypes);
                                break;
                            }
                        }
                    }
                }
            }

            if (result == null) {
                // проход по иерархии матриц, связанных через martix-reference-field, для матрицы переданного доменного
                // объекта
                if (accessMatrixConfig != null && accessMatrixConfig.getMatrixReference() != null) {
                    String matrixReferenceTypeName = getParentTypeNameFromMatrixReference(accessMatrixConfig.getMatrixReference(), domainObjectTypeConfig);
                    if (matrixReferenceTypeName != null && !visitedTypes.contains(matrixReferenceTypeName)) {
                        result = isReadEverybodyForType(matrixReferenceTypeName, visitedTypes);
                    }
                }
            }
        }
        return result;
    }

    private void fillEventLogDomainObjectAccessConfig() {
        EventLogsConfig eventLogsConfiguration = configurationExplorer.getEventLogsConfiguration();
        if (eventLogsConfiguration == null || eventLogsConfiguration.getDomainObjectAccess() == null
                || !eventLogsConfiguration.getDomainObjectAccess().isEnable()) return;

        DomainObjectAccessConfig domainObjectAccessConfig = eventLogsConfiguration.getDomainObjectAccess();
        List<LogDomainObjectAccessConfig> logs = domainObjectAccessConfig.getLogs();
        if (logs != null) {
            for (LogDomainObjectAccessConfig log : logs) {
                List<DomainObjectTypeConfig> domainObjectTypeConfigList = log.getDomainObjectTypeConfigList();
                if (domainObjectTypeConfigList != null){
                    for (DomainObjectTypeConfig objectTypeConfig : domainObjectTypeConfigList) {
                        configurationStorage.eventLogDomainObjectAccessConfig.put(objectTypeConfig.getName(), log);
                    }
                }
            }
        }

        // если настройка на произвольный тип ДО не указана явно, то по умолчанию логгируем все события
        if (!configurationStorage.eventLogDomainObjectAccessConfig.containsKey("*")){
            LogDomainObjectAccessConfig allEvents = new LogDomainObjectAccessConfig();
            allEvents.setEnable(true);
            allEvents.setAccessWasGranted("*");
            allEvents.setAccessType("*");
            configurationStorage.eventLogDomainObjectAccessConfig.put("*", allEvents);
        }

    }



    @Deprecated
    private void fillReadPermittedToEverybodyMapFromStatus(AccessMatrixConfig accessMatrixConfig) {
        for (AccessMatrixStatusConfig accessMatrixStatus : accessMatrixConfig.getStatus()) {
            if (ALL_STATUSES_SIGN.equals(accessMatrixStatus.getName()))
                for (BaseOperationPermitConfig permission : accessMatrixStatus.getPermissions()) {

                    if (ReadConfig.class.equals(permission.getClass())
                            && (Boolean.TRUE.equals(((ReadConfig) permission).isPermitEverybody()))) {
                        configurationStorage.readPermittedToEverybodyMap.put(accessMatrixConfig.getType(), true);
                        return;
                    }
                }
            configurationStorage.readPermittedToEverybodyMap.put(accessMatrixConfig.getType(), false);
        }
    }

    private void lock() {
        writeLock.lock();
    }

    private void unlock() {
        writeLock.unlock();
    }

    private class TypesDelegatingAccessCheckToBuilderData {
        private Set<String> hierarchicalDelegates = new HashSet<>();
        private Set<String> hierarchicalDelegatesInLowerCase = new HashSet<>();
        private Set<String> matrixDelegates = new HashSet<>();
        private Set<String> matrixDelegatesInLowerCase = new HashSet<>();

        public Set<String> getHierarchicalDelegates() {
            return hierarchicalDelegates;
        }

        public Set<String> getHierarchicalDelegatesInLowerCase() {
            return hierarchicalDelegatesInLowerCase;
        }

        public Set<String> getMatrixDelegates() {
            return matrixDelegates;
        }

        public Set<String> getMatrixDelegatesInLowerCase() {
            return matrixDelegatesInLowerCase;
        }
    }

    private class AttachmentPrototypeHelper {

        public DomainObjectTypeConfig makeAttachmentConfig(String templateName, String name, String ownerTypeName)
                throws IOException, ClassNotFoundException {
            DomainObjectTypeConfig cloneDomainObjectTypeConfig = new DomainObjectTypeConfig();

            cloneDomainObjectTypeConfig.setName(name);
            cloneDomainObjectTypeConfig.setTemplate(false);

            collectFieldsConfig(cloneDomainObjectTypeConfig, templateName);

            ReferenceFieldConfig ownerReferenceConfig = new ReferenceFieldConfig();
            ownerReferenceConfig.setName(ownerTypeName);
            ownerReferenceConfig.setType(ownerTypeName);
            cloneDomainObjectTypeConfig.getFieldConfigs().add(ownerReferenceConfig);

            return cloneDomainObjectTypeConfig;
        }

        private void collectFieldsConfig(DomainObjectTypeConfig cloneDomainObjectTypeConfig, String templateName) {
            if (templateName == null) {
                templateName = GenericDomainObject.ATTACHMENT_TEMPLATE;
            }
            DomainObjectTypeConfig templateDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, templateName);
            if (templateDomainObjectTypeConfig == null) {
                throw new FatalException("Attachment template: " + templateName + " not found in configuration");
            }
            cloneDomainObjectTypeConfig.getFieldConfigs().addAll(templateDomainObjectTypeConfig.getFieldConfigs());
            cloneDomainObjectTypeConfig.getUniqueKeyConfigs().addAll(templateDomainObjectTypeConfig.getUniqueKeyConfigs());
            cloneDomainObjectTypeConfig.setIndicesConfig(templateDomainObjectTypeConfig.getIndicesConfig());

            if (templateDomainObjectTypeConfig.getExtendsAttribute() != null) {
                collectFieldsConfig(cloneDomainObjectTypeConfig, templateDomainObjectTypeConfig.getExtendsAttribute());
            }
        }

    }

}
