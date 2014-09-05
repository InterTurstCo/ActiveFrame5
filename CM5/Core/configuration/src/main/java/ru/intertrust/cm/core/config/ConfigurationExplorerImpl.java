package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 * @author vmatsukevich Date: 6/12/13 Time: 5:21 PM
 */
public class ConfigurationExplorerImpl implements ConfigurationExplorer, ApplicationEventPublisherAware {

    private final static Logger logger = LoggerFactory.getLogger(ConfigurationExplorerImpl.class);

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock  = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private ConfigurationStorage configStorage;
    private ConfigurationStorageBuilder configurationStorageBuilder;

    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    FormLogicalValidator formLogicalValidator;

    @Autowired
    NavigationPanelLogicalValidator navigationPanelLogicalValidator;

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
        validateGui();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        readLock.lock();
        try {
            return getReturnObject(configStorage.configuration, Configuration.class);
        } finally {
            readLock.unlock();
        }
    }

    public GlobalSettingsConfig getGlobalSettings() {
        readLock.lock();
        try {
            return getReturnObject(configStorage.globalSettings, GlobalSettingsConfig.class);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Каждый логический валидатор находится в блоке try/catch для отображения всех ошибок, возникнувших в результате
     * валидации, а не только первого бросившего exception
     *
     */
    private void validate() {
        new GlobalSettingsLogicalValidator(configStorage.configuration).validate();
        new DomainObjectLogicalValidator(this).validate();
        if (configStorage.globalSettings.validateAccessMatrices()) {
            new AccessMatrixLogicalValidator(this).validate();
        }
        new UniqueNameLogicalValidator(this).validate();
        if (configStorage.globalSettings.validateIndirectPermissions()) {
            new IndirectlyPermissionLogicalValidator(this).validate();
        }
        new ReadEvrybodyPermissionLogicalValidator(this).validate();
    }

    public void validateGui() {
        StringBuilder errorLogBuilder = new StringBuilder();

        try {
            navigationPanelLogicalValidator.setConfigurationExplorer(this);
            navigationPanelLogicalValidator.validate();
        } catch (ConfigurationException e) {
            errorLogBuilder.append(e.getMessage());
        }

        try {
            formLogicalValidator.setConfigurationExplorer(this);
            formLogicalValidator.validate();
        } catch (ConfigurationException e) {
            errorLogBuilder.append(e.getMessage());
        }

        try {
            CollectionViewLogicalValidator collectionLogicalValidator = new CollectionViewLogicalValidator(this);
            collectionLogicalValidator.validate();
        } catch (ConfigurationException e) {
            errorLogBuilder.append(e.getMessage());
        }

        String errorLog = errorLogBuilder.toString();
        if (errorLog.length() > 0){
            throw new ConfigurationException(errorLog);
        }

        logger.info("GUI configuration has passed logical validation");
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
            return getReturnObject(config, type);
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

            return getReturnObject(result, ArrayList.class);
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
                return new ArrayList<>();
            }

            List<DomainObjectTypeConfig> result = new ArrayList<>();
            result.addAll(childTypes);

            return getReturnObject(result, ArrayList.class);
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
                return getReturnObject(result, result.getClass());
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
            return getReturnObject(collectionColumnConfig, CollectionColumnConfig.class);
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

            if (dynamicGroups == null) {
                return new ArrayList<>();
            }

            List<DynamicGroupConfig> result = new ArrayList<>();
            result.addAll(dynamicGroups);

            return getReturnObject(result, ArrayList.class);
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

            List<DynamicGroupConfig> result = new ArrayList<>(dynamicGroups.size());
            result.addAll(dynamicGroups);
            return getReturnObject(result, ArrayList.class);
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
            if (status == null) {
                status = "*";
            }

            FieldConfigKey key = new FieldConfigKey(domainObjectType, status);
            AccessMatrixStatusConfig result = configStorage.accessMatrixByObjectTypeAndStatusMap.get(key);

            if (result == null) {
                result = configurationStorageBuilder.fillAccessMatrixByObjectTypeAndStatus(domainObjectType, status);
            }

            return getReturnObject(result, AccessMatrixStatusConfig.class);
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
            //Получение конфигурации матрицы, здесь НЕЛЬЗЯ учитывать наследование, так как вызывающие методы должны получить матрицу непосредственно для переданного типа
            AccessMatrixConfig accessMatrixConfig = getConfig(AccessMatrixConfig.class, domainObjectType);
            return getReturnObject(accessMatrixConfig, AccessMatrixConfig.class);
        } finally {
            readLock.unlock();
        }
    }

    public AccessMatrixConfig getAccessMatrixByObjectTypeUsingExtention(String domainObjectType) {
        AccessMatrixConfig accessMatrixConfig = getConfig(AccessMatrixConfig.class, domainObjectType);

        if (accessMatrixConfig != null) {
            return accessMatrixConfig;
        } else {
            DomainObjectTypeConfig domainObjectTypeConfig =
                    getConfig(DomainObjectTypeConfig.class, domainObjectType);
            if (domainObjectTypeConfig != null && domainObjectTypeConfig.getExtendsAttribute() != null) {
                String parentDOType = domainObjectTypeConfig.getExtendsAttribute();
                return getAccessMatrixByObjectTypeUsingExtention(parentDOType);
            }
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
            if (configStorage.readPermittedToEverybodyMap.get(domainObjectType) != null) {
                return configStorage.readPermittedToEverybodyMap.get(domainObjectType);
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Получение имени типа доменного объекта, который необходимо использовать при вычисление прав на доменный объект в случае
     * использования заимствования прав у связанного объекта
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

            return getReturnObject(result, String.class);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public ToolBarConfig getDefaultToolbarConfig(String pluginName) {
        readLock.lock();
        try {
            ToolBarConfig toolBarConfig = configStorage.toolbarConfigByPluginMap.get(pluginName);
            return getReturnObject(toolBarConfig, ToolBarConfig.class);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public SqlTrace getSqlTraceConfiguration() {
        readLock.lock();
        try {
            return getReturnObject(configStorage.sqlTrace, SqlTrace.class);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getDomainObjectParentType(String typeName) {
        readLock.lock();
        try {
            String[] typesHierarchy = getDomainObjectTypesHierarchy(typeName);

            if (typesHierarchy == null || typesHierarchy.length == 0){
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

            if (typesHierarchy == null || typesHierarchy.length == 0){
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
            if (this.configStorage.domainObjectTypesHierarchy.containsKey(typeName)){
                return getReturnObject(this.configStorage.domainObjectTypesHierarchy.get(typeName), String[].class);
            } else {
                return getReturnObject(configurationStorageBuilder.fillDomainObjectTypesHierarchyMap(typeName), String[].class);
            }
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

    private <T> T getReturnObject(Object source, Class<T> tClass) {
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

        AccessMatrixConfig accessMatrix = getAccessMatrixByObjectTypeUsingExtention(objectType);

        if (accessMatrix != null && accessMatrix.getCreateConfig() != null && accessMatrix.getCreateConfig().getPermitGroups() != null) {
            for (PermitGroup permitGroup : accessMatrix.getCreateConfig().getPermitGroups()) {
                userGroups.add(permitGroup.getName());
            }
        }
        return userGroups;
    }

}
