package ru.intertrust.cm.core.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.eventlog.DomainObjectAccessConfig;
import ru.intertrust.cm.core.config.eventlog.EventLogsConfig;
import ru.intertrust.cm.core.config.eventlog.LogDomainObjectAccessConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.ObjectCloner;

public class ConfigurationStorageBuilder {
    private static Logger log = LoggerFactory.getLogger(ConfigurationStorageBuilder.class);

    private static final String ALL_STATUSES_SIGN = "*";
    private final static String GLOBAL_SETTINGS_CLASS_NAME = "ru.intertrust.cm.core.config.GlobalSettingsConfig";

    private ConfigurationExplorer configurationExplorer;
    private ConfigurationStorage configurationStorage;

    public ConfigurationStorageBuilder(ConfigurationExplorer configurationExplorer, ConfigurationStorage configurationStorage) {
        this.configurationExplorer = configurationExplorer;
        this.configurationStorage = configurationStorage;
    }

    public void buildConfigurationStorage() {
        initConfigurationMaps();
    }

    public void fillTopLevelConfigMap(TopLevelConfig config) {
        CaseInsensitiveMap<TopLevelConfig> typeMap = configurationStorage.topLevelConfigMap.get(config.getClass());
        if (typeMap == null) {
            typeMap = new CaseInsensitiveMap<>();
            configurationStorage.topLevelConfigMap.put(config.getClass(), typeMap);
        }
        typeMap.put(config.getName(), config);
    }

    public void fillGlobalSettingsCache(TopLevelConfig config) {
        if (GLOBAL_SETTINGS_CLASS_NAME.equalsIgnoreCase(config.getClass().getCanonicalName())) {
            configurationStorage.globalSettings = (GlobalSettingsConfig) config;
        }
    }

    public void fillCollectionColumnConfigMap(CollectionViewConfig collectionViewConfig) {
        if (collectionViewConfig.getCollectionDisplayConfig() != null) {
            for (CollectionColumnConfig columnConfig : collectionViewConfig.getCollectionDisplayConfig().
                    getColumnConfig()) {
                FieldConfigKey fieldConfigKey =
                        new FieldConfigKey(collectionViewConfig.getName(), columnConfig.getField());
                configurationStorage.collectionColumnConfigMap.put(fieldConfigKey, columnConfig);
            }
        }
    }

    public void updateCollectionColumnConfigMap(CollectionViewConfig oldConfig, CollectionViewConfig newConfig) {
        if (oldConfig != null && oldConfig.getCollectionDisplayConfig() != null) {
            for (CollectionColumnConfig columnConfig : oldConfig.getCollectionDisplayConfig().getColumnConfig()) {
                FieldConfigKey fieldConfigKey = new FieldConfigKey(oldConfig.getName(), columnConfig.getField());
                configurationStorage.collectionColumnConfigMap.remove(fieldConfigKey);
            }
        }

        fillCollectionColumnConfigMap(newConfig);
    }

    public void updateToolbarConfigByPluginMap(ToolBarConfig oldConfig, ToolBarConfig newConfig) {
        if (oldConfig != null) {
            configurationStorage.toolbarConfigByPluginMap.remove(oldConfig.getPlugin());
        }

        fillToolbarConfigByPluginMap(newConfig);
    }

    public void fillToolbarConfigByPluginMap(ToolBarConfig toolBarConfig) {
        if (configurationStorage.toolbarConfigByPluginMap.get(toolBarConfig.getPlugin()) == null) {
            configurationStorage.toolbarConfigByPluginMap.put(toolBarConfig.getPlugin(), toolBarConfig);
        }
    }

    public List<DynamicGroupConfig> fillDynamicGroupConfigContextMap(String domainObjectType) {
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

        configurationStorage.dynamicGroupConfigByContextMap.put(domainObjectType, dynamicGroups);
        return dynamicGroups;
    }

    public List<DynamicGroupConfig> fillDynamicGroupConfigsByTrackDOMap(String trackDOTypeName, String status) {
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

        configurationStorage.dynamicGroupConfigsByTrackDOMap.put(new FieldConfigKey(trackDOTypeName, status), resultList);
        return resultList;
    }

    public AccessMatrixStatusConfig fillAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status) {
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

        configurationStorage.accessMatrixByObjectTypeAndStatusMap.put(new FieldConfigKey(domainObjectType, status), result);
        return result;
    }

    public String fillMatrixReferenceTypeNameMap(String childTypeName) {
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

        if (result != null) {
            configurationStorage.matrixReferenceTypeNameMap.put(childTypeName, result);
        }

        return result;
    }

    public String[] fillDomainObjectTypesHierarchyMap(String typeName) {
        List<String> typesHierarchy = new ArrayList<>();
        buildDomainObjectTypesHierarchy(typesHierarchy, typeName);
        Collections.reverse(typesHierarchy);
        String[] types = typesHierarchy.toArray(new String[typesHierarchy.size()]);
        configurationStorage.domainObjectTypesHierarchy.put(typeName, types);
        return types;
    }

    public void updateDomainObjectFieldConfig(DomainObjectTypeConfig oldType, DomainObjectTypeConfig newType) {
        if (oldType != null) {
            removeDomainObjectFieldConfigsFromMap(oldType);
        }
        fillDomainObjectFieldConfig(newType);
    }

    public void updateConfigurationMapOfChildDomainObjectType(DomainObjectTypeConfig type) {
        String typeName = type.getName();

        configurationStorage.directChildDomainObjectTypesMap.remove(typeName);
        configurationStorage.indirectChildDomainObjectTypesMap.remove(typeName);

        fillConfigurationMapOfChildDomainObjectType(type);
    }

    public void updateConfigurationMapsOfAttachmentDomainObjectType(DomainObjectTypeConfig oldConfig,
                                                                    DomainObjectTypeConfig newConfig) {
        try {
            if (oldConfig != null && oldConfig.getAttachmentTypesConfig() != null) {
                AttachmentPrototypeHelper attachmentPrototypeHelper = new AttachmentPrototypeHelper();
                for (AttachmentTypeConfig attachmentTypeConfig :
                        oldConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs()) {
                    DomainObjectTypeConfig attachmentDomainObjectTypeConfig =
                            attachmentPrototypeHelper.makeAttachmentConfig(attachmentTypeConfig.getName(), oldConfig.getName());

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
        }
    }

    public void updateAuditLogConfigs(DomainObjectTypeConfig oldConfig, DomainObjectTypeConfig newConfig) {
        DomainObjectTypeConfig oldAuditLogConfig = createAuditLogConfig(oldConfig);
        removeTopLevelConfigFromMap(oldAuditLogConfig);
        removeDomainObjectFieldConfigsFromMap(oldAuditLogConfig);
        configurationStorage.auditLogTypes.remove(oldAuditLogConfig.getName());
        
        fillAuditLogConfigMap(newConfig);
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
        configurationStorage.directChildDomainObjectTypesMap.put(typeName, directChildTypes);
        configurationStorage.indirectChildDomainObjectTypesMap.put(typeName, indirectChildTypes);
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
        
        for (FieldConfig fieldConfig : allFieldsConfig) {
            FieldConfigKey fieldConfigKey =
                    new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
            configurationStorage.fieldConfigMap.put(fieldConfigKey, fieldConfig);
        }
        fillSystemFields(domainObjectTypeConfig);
    }

    private void fillAuditLogConfigMap(DomainObjectTypeConfig domainObjectTypeConfig) {
        if (domainObjectTypeConfig == null) {
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
        }
        
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            ObjectCloner cloner = new ObjectCloner();
            final FieldConfig clonedConfig = cloner.cloneObject(fieldConfig, fieldConfig.getClass());
            clonedConfig.setNotNull(false);
            auditLogDomainObjectConfig.getFieldConfigs().add(clonedConfig);

        }
        return auditLogDomainObjectConfig;
    }

    public static String getALTableName(String name) {
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
                        attachmentPrototypeHelper.makeAttachmentConfig(attachmentTypeConfig.getName(),
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
        for (TopLevelConfig config : configurationExplorer.getConfigs(DomainObjectTypeConfig.class)) {
            boolean readEverybody = isReadEverybodyForType(config.getName());

            //Если readEverybody == false Дополнительная проверка на заимствование прав
            if (!readEverybody) {
                AccessMatrixConfig accessMatrixConfig =
                        configurationExplorer.getAccessMatrixByObjectType(config.getName());
                if (accessMatrixConfig != null && accessMatrixConfig.getMatrixReference() != null) {

                    //Получение типа откуда заимствуем права и проверяем у полученного типа флаг ReadEverybody 
                    String matrixReferenceType = fillMatrixReferenceTypeNameMap(config.getName());
                    if (matrixReferenceType == null) {
                        continue;  // todo: throw exception
                    }
                    readEverybody = isReadEverybodyForType(matrixReferenceType);
                }
            }

            configurationStorage.readPermittedToEverybodyMap.put(config.getName(), readEverybody);
        }
    }

    /**
     * Получение флага ReadEverybody для типа с учетом иерархии типов
     * @param domainObjectType
     * @return
     */
    private boolean isReadEverybodyForType(String domainObjectType) {
        boolean result = false;

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
            if (domainObjectTypeConfig.getExtendsAttribute() != null) {
                String parentDOType = domainObjectTypeConfig.getExtendsAttribute();
                result = isReadEverybodyForType(parentDOType);
            }else{
                //domainObjectType является ДО верхнего уровня, и флаг read-everybody не определен.
                //Получаем все дочерние типы и смотрим флаг у них. Возвращаем true если найден хотя бы один тип с флагом read-evrybody
                //Валидатор конфигурации должен обеспечить чтобы если встречается хотя бы один тип с read-everybody то должна отсутствовать матрица у типов в данной иерархии с read-everybody = false
                List<String> childTypes = getChildTypes(domainObjectType);
                for (String childType : childTypes) {
                    AccessMatrixConfig childMatrixConfig = configurationExplorer.getAccessMatrixByObjectType(childType);
                    if (childMatrixConfig != null && childMatrixConfig.isReadEverybody() != null){
                        result = childMatrixConfig.isReadEverybody();
                        break;
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

    private class PrototypeHelper {
        private ByteArrayInputStream bis;

        private PrototypeHelper(String templateName) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            TopLevelConfig templateDomainObjectTypeConfig = configurationExplorer.getConfig(DomainObjectTypeConfig.class, templateName);
            oos.writeObject(templateDomainObjectTypeConfig);
            oos.close();
            bis = new ByteArrayInputStream(bos.toByteArray());
        }

        public DomainObjectTypeConfig makeDomainObjectTypeConfig(String name)
                throws IOException, ClassNotFoundException {
            bis.reset();
            DomainObjectTypeConfig cloneDomainObjectTypeConfig =
                    (DomainObjectTypeConfig) new ObjectInputStream(bis).readObject();
            cloneDomainObjectTypeConfig.setTemplate(false);
            cloneDomainObjectTypeConfig.setName(name);

            return cloneDomainObjectTypeConfig;
        }
    }

    private class AttachmentPrototypeHelper {
        private PrototypeHelper prototypeHelper;

        private AttachmentPrototypeHelper() throws IOException {
            prototypeHelper = new PrototypeHelper("Attachment");
        }

        public DomainObjectTypeConfig makeAttachmentConfig(String name, String ownerTypeName)
                throws IOException, ClassNotFoundException {
            DomainObjectTypeConfig cloneDomainObjectTypeConfig = prototypeHelper.makeDomainObjectTypeConfig(name);

            ReferenceFieldConfig ownerReferenceConfig = new ReferenceFieldConfig();
            ownerReferenceConfig.setName(ownerTypeName);
            ownerReferenceConfig.setType(ownerTypeName);
            cloneDomainObjectTypeConfig.getFieldConfigs().add(ownerReferenceConfig);

            return cloneDomainObjectTypeConfig;
        }
    }

}
