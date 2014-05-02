package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.KryoCloner;

import java.io.*;
import java.util.*;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 * @author vmatsukevich Date: 6/12/13 Time: 5:21 PM
 */
public class ConfigurationExplorerImpl implements ConfigurationExplorer {
    private final static Logger logger = LoggerFactory.getLogger(ConfigurationExplorerImpl.class);

    private ConfigurationStorage configStorage;

    @Autowired
    FormLogicalValidator formLogicalValidator;

    @Autowired
    NavigationPanelLogicalValidator navigationPanelLogicalValidator;

    private KryoCloner kryoCloner = new KryoCloner();

    /**
     * Создает {@link ConfigurationExplorerImpl}
     */
    public ConfigurationExplorerImpl(Configuration configuration) {
        new ConfigurationExplorerBuilder().buildConfigurationStorage(this, configuration);
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        Configuration cloneConfiguration = kryoCloner.cloneObject(configStorage.configuration, Configuration.class);
        return cloneConfiguration;
    }

    public GlobalSettingsConfig getGlobalSettings() {
        return kryoCloner.cloneObject(configStorage.globalSettings, GlobalSettingsConfig.class) ;
    }

    /**
     * Каждый логический валидатор находится в блоке try/catch для отображения всех ошибок, возникнувших в результате
     * валидации, а не только первого бросившего exception
     *
     */
    private void validate() {
        GlobalSettingsLogicalValidator globalSettingsLogicalValidator =
                new GlobalSettingsLogicalValidator(configStorage.configuration);
        globalSettingsLogicalValidator.validate();
        DomainObjectLogicalValidator domainObjectLogicalValidator = new DomainObjectLogicalValidator(this);
        domainObjectLogicalValidator.validate();
        AccessMatrixLogicalValidator accessMatrixLogicalValidator = new AccessMatrixLogicalValidator(this);
        accessMatrixLogicalValidator.validate();
    }

    public void validateGui() {
        try {
            navigationPanelLogicalValidator.setConfigurationExplorer(this);
            navigationPanelLogicalValidator.validate();
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
        }
        try {
            formLogicalValidator.setConfigurationExplorer(this);
            formLogicalValidator.validate();
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
        }

        try {
            CollectionViewLogicalValidator collectionLogicalValidator = new CollectionViewLogicalValidator(this);
            collectionLogicalValidator.validate();
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfig(Class<T> type, String name) {
        CaseInsensitiveMap<TopLevelConfig> typeMap = configStorage.topLevelConfigMap.get(type);
        if (typeMap == null) {
            return null;
        }

        T config = (T) typeMap.get(name);
        return kryoCloner.cloneObject(config, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getConfigs(Class<T> type) {
        CaseInsensitiveMap<TopLevelConfig> typeMap = configStorage.topLevelConfigMap.get(type);
        if (typeMap == null) {
            return Collections.EMPTY_LIST;
        }

        //Перекладываем в другой контейнер, для возможности сериализации
        List<T> result = new ArrayList<T>();
        result.addAll((Collection<T>) typeMap.values());

        return kryoCloner.cloneObject(result, ArrayList.class);
    }

    @Override
    public DomainObjectTypeConfig getDomainObjectTypeConfig(String typeName) {
        return getConfig(DomainObjectTypeConfig.class, typeName);
    }

    @Override
    public Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect) {
        ArrayList<DomainObjectTypeConfig> childTypes = new ArrayList<>();
        Collection<DomainObjectTypeConfig> allTypes = getConfigs(DomainObjectTypeConfig.class);
        for (DomainObjectTypeConfig type : allTypes) {
            if (typeName.equals(type.getExtendsAttribute())) {
                childTypes.add(kryoCloner.cloneObject(type, type.getClass()));
                if (includeIndirect) {
                    childTypes.addAll(findChildDomainObjectTypes(type.getName(), true));
                }
            }
        }
        return childTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        return getFieldConfig(domainObjectConfigName, fieldConfigName, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName,
            boolean returnInheritedConfig) {
        if (REFERENCE_TYPE_ANY.equals(domainObjectConfigName)) {
            throw new IllegalArgumentException("'*' is not a valid Domain Object type");
        }

        FieldConfigKey fieldConfigKey = new FieldConfigKey(domainObjectConfigName, fieldConfigName);
        FieldConfig result = configStorage.fieldConfigMap.get(fieldConfigKey);

        if (result != null) {
            return kryoCloner.cloneObject(result, result.getClass());
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionColumnConfig getCollectionColumnConfig(String collectionViewName, String columnConfigName) {
        FieldConfigKey collectionColumnConfigKey = new FieldConfigKey(collectionViewName, columnConfigName);
        CollectionColumnConfig collectionColumnConfig = configStorage.collectionColumnConfigMap.get(collectionColumnConfigKey);
        return kryoCloner.cloneObject(collectionColumnConfig, collectionColumnConfig.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType) {
        List<DynamicGroupConfig> dynamicGroups = new ArrayList<>();

        CaseInsensitiveMap<TopLevelConfig> dynamicGroupMap = configStorage.topLevelConfigMap.get(DynamicGroupConfig.class);

        for (String groupKey : dynamicGroupMap.keySet()) {
            DynamicGroupConfig dynamicGroup = (DynamicGroupConfig) dynamicGroupMap.get(groupKey);
            if (dynamicGroup.getContext() != null && dynamicGroup.getContext().getDomainObject() != null) {
                String objectType = dynamicGroup.getContext().getDomainObject().getType();

                if (objectType.equals(domainObjectType)) {
                    dynamicGroups.add(kryoCloner.cloneObject(dynamicGroup, dynamicGroup.getClass()));
                }
            }
        }

        return dynamicGroups;
    }

    /**
     * {@inheritDoc}
     */
    public List<DynamicGroupConfig> getDynamicGroupConfigsByTrackDO(String trackDOTypeName, String status) {
        List<DynamicGroupConfig> dynamicGroups = new ArrayList<DynamicGroupConfig>();

        CaseInsensitiveMap<TopLevelConfig> dynamicGroupMap = configStorage.topLevelConfigMap.get(DynamicGroupConfig.class);

        for (String groupKey : dynamicGroupMap.keySet()) {
            DynamicGroupConfig dynamicGroup = (DynamicGroupConfig) dynamicGroupMap.get(groupKey);

            if (dynamicGroup.getMembers() != null && dynamicGroup.getMembers().getTrackDomainObjects() != null) {
                List<DynamicGroupTrackDomainObjectsConfig> trackDomainObjectConfigs =
                        dynamicGroup.getMembers().getTrackDomainObjects();
                for (DynamicGroupTrackDomainObjectsConfig trackDomainObjectConfig : trackDomainObjectConfigs) {
                    String configuredStatus = trackDomainObjectConfig.getStatus();
                    String configuredType = trackDomainObjectConfig.getType();
                    if (trackDOTypeName.equalsIgnoreCase(configuredType)) {

                        if (configuredStatus == null || configuredStatus.equals(status)) {
                            dynamicGroups.add(kryoCloner.cloneObject(dynamicGroup, dynamicGroup.getClass()));
                        }
                    }
                }
            }
        }
        return dynamicGroups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessMatrixStatusConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status) {
    	AccessMatrixStatusConfig result = null;
    	//Получение конфигурации матрицы
    	AccessMatrixConfig accessMatrixConfig = getConfig(AccessMatrixConfig.class, domainObjectType);
    	if (accessMatrixConfig != null && accessMatrixConfig.getStatus() != null) {

    		//Получаем все статусы
    		for (AccessMatrixStatusConfig accessStatusConfig : accessMatrixConfig.getStatus()) {
    			//Если статус в конфигурации звезда то не проверяем статусы на соответствие, а возвращаем текущий
    			if (accessStatusConfig.getName().equals("*")){
    				result = kryoCloner.cloneObject(accessStatusConfig, accessStatusConfig.getClass());
    				break;
    			}else if (status != null && status.equalsIgnoreCase(accessStatusConfig.getName())) {
    				result = kryoCloner.cloneObject(accessStatusConfig, accessStatusConfig.getClass());
    				break;
    			}
    		}
    	}

    	return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessMatrixConfig getAccessMatrixByObjectType(String domainObjectType) {
        //Получение конфигурации матрицы
        AccessMatrixConfig accessMatrixConfig = getConfig(AccessMatrixConfig.class, domainObjectType);
        return accessMatrixConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttachmentType(String domainObjectType) {
        return configStorage.attachmentDomainObjectTypes.containsKey(domainObjectType);
    }

    public boolean isReadPermittedToEverybody(String domainObjectType) {
        if (configStorage.readPermittedToEverybodyMap.get(domainObjectType) != null) {
            return configStorage.readPermittedToEverybodyMap.get(domainObjectType);
        }
        return false;
    }

    /**
     * Получение имени типа доменного объекта, который необходимо использовать при вычисление прав на доменный объект в случае
     * использования заимствования прав у связанного объекта
     * @param childTypeName имя типа, для которого необходимо вычислить тип объекта из которого заимствуются права
     * @return имя типа у которого заимствуются права или null в случае если заимствования нет
     */
    @Override
    public String getMatrixReferenceTypeName(String childTypeName) {
        //Получаем матрицу и смотрим атрибут matrix_reference_field
        AccessMatrixConfig matrixConfig = null;
        DomainObjectTypeConfig childDomainObjectTypeConfig = getConfig(DomainObjectTypeConfig.class, childTypeName);

        //Ищим матрицу для типа с учетом иерархии типов
        while((matrixConfig = getAccessMatrixByObjectType(childDomainObjectTypeConfig.getName())) == null
                && childDomainObjectTypeConfig.getExtendsAttribute() != null){
            childDomainObjectTypeConfig = getConfig(DomainObjectTypeConfig.class, childDomainObjectTypeConfig.getExtendsAttribute());
        }
        String result = null;
        if (matrixConfig != null && matrixConfig.getMatrixReference() != null){
            //Получаем имя типа на которого ссылается martix-reference-field
            String parentTypeName = getParentTypeNameFromMatrixReference(matrixConfig.getMatrixReference(), childDomainObjectTypeConfig);
            //Вызываем рекурсивно метод для родительского типа, на случай если в родительской матрице так же заполнено поле martix-reference-field
            result = getMatrixReferenceTypeName(parentTypeName);
            //В случае если у родителя не заполнен атрибут martix-reference-field то возвращаем имя родителя
            if (result == null){
                result = parentTypeName;
            }
        }
        return result;
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
        if (matrixReferenceFieldName.indexOf(".") > 0){
            // TODO здесь надо добавить обработку backlink
            throw new UnsupportedOperationException("Not implemented access referencing using backlink.");
        }else{
            ReferenceFieldConfig fieldConfig =  (ReferenceFieldConfig)getFieldConfig(domainObjectTypeConfig.getName(), matrixReferenceFieldName);
            result = fieldConfig.getType();
        }
        return result;
    }

    /**
     * @inheritDoc
     */
    @Override
    public ToolBarConfig getDefaultToolbarConfig(String pluginName) {
        final Collection<ToolBarConfig> configs = getConfigs(ToolBarConfig.class);
        for (ToolBarConfig config : configs) {
            if (config.getPlugin().equals(pluginName)) {
                return config;
            }
        }
        return null;
    }


    @Override
    public String getDomainObjectParentType(String typeName) {
        String[] typesHierarchy = getDomainObjectTypesHierarchy(typeName);

        if (typesHierarchy == null || typesHierarchy.length==0){
            return null;
        }

        return typesHierarchy[typesHierarchy.length - 1];
    }

    @Override
    public String getDomainObjectRootType(String typeName) {
        String[] typesHierarchy = getDomainObjectTypesHierarchy(typeName);

        if (typesHierarchy == null || typesHierarchy.length == 0){
            return typeName;
        }

        return typesHierarchy[0];
    }

    @Override
    public String[] getDomainObjectTypesHierarchy(String typeName) {
        if (this.configStorage.domainObjectTypesHierarchy.containsKey(typeName)){
            return this.configStorage.domainObjectTypesHierarchy.get(typeName);
        }
        List <String> typesHierarchy = new ArrayList<>();
        buildDomainObjectTypesHierarchy(typesHierarchy, typeName);
        Collections.reverse(typesHierarchy);
        String[] types = typesHierarchy.toArray(new String[typesHierarchy.size()]);
        this.configStorage.domainObjectTypesHierarchy.put(typeName, types);
        return types;
    }

    private void buildDomainObjectTypesHierarchy(List<String> typesHierarchy, String typeName) {
        DomainObjectTypeConfig domainObjectTypeConfig = getDomainObjectTypeConfig(typeName);
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


    ConfigurationStorage getConfig() {
        return configStorage;
    }

    void setConfig(ConfigurationStorage config) {
        this.configStorage = config;
    }
}
