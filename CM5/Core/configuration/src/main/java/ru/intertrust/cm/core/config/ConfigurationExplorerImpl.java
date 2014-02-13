package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveMap;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.util.KryoCloner;

import java.io.*;
import java.util.*;

/**
 * Предоставляет быстрый доступ к элементам конфигурации. После создания объекта данного класса требуется выполнить
 * инициализацию через вызов метода {@link #build()}.
 * @author vmatsukevich Date: 6/12/13 Time: 5:21 PM
 */
public class ConfigurationExplorerImpl implements ConfigurationExplorer {
    private static final String ALL_STATUSES_SIGN = "*";
    private final static Logger logger = LoggerFactory.getLogger(ConfigurationExplorerImpl.class);
    private final static String GLOBAL_SETTINGS_CLASS_NAME = "ru.intertrust.cm.core.config.GlobalSettingsConfig";
    private Configuration configuration;

    private Map<Class<?>, CaseInsensitiveMap<TopLevelConfig>> topLevelConfigMap = new HashMap<>();
    private Map<FieldConfigKey, FieldConfig> fieldConfigMap = new HashMap<>();
    private Map<FieldConfigKey, CollectionColumnConfig> collectionColumnConfigMap = new HashMap<>();
    
    private Map<String, Boolean> readPermittedToEverybodyMap = new HashMap<>();
    
    private GlobalSettingsConfig globalSettings;
    private CaseInsensitiveMap<String> attachmentDomainObjectTypes = new CaseInsensitiveMap<>();
    @Autowired
    FormLogicalValidator formLogicalValidator;

    @Autowired
    NavigationPanelLogicalValidator navigationPanelLogicalValidator;

    private KryoCloner kryoCloner = new KryoCloner();

    /**
     * Создает {@link ConfigurationExplorerImpl}
     */
    public ConfigurationExplorerImpl(Configuration configuration) {
        this.configuration = configuration;
        build();
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {

        Configuration cloneConfiguration = kryoCloner.cloneObject(configuration, Configuration.class);
        return cloneConfiguration;
    }

    /**
     * Устанавливает конфигурацию
     * @param configuration
     *            конфигурация
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public GlobalSettingsConfig getGlobalSettings() {
        return kryoCloner.cloneObject(globalSettings, GlobalSettingsConfig.class) ;
    }

    /**
     * {@inheritDoc}
     */
    public void build() {
        initConfigurationMaps();
    }

    /**
     * Каждый логический валидатор находится в блоке try/catch для отображения всех ошибок, возникнувших в результате
     * валидации, а не только первого бросившего exception
     *
     */
    private void validate() {
        GlobalSettingsLogicalValidator globalSettingsLogicalValidator =
                new GlobalSettingsLogicalValidator(configuration);
        globalSettingsLogicalValidator.validate();
        DomainObjectLogicalValidator domainObjectLogicalValidator = new DomainObjectLogicalValidator(this);
        domainObjectLogicalValidator.validate();

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
        CaseInsensitiveMap<TopLevelConfig> typeMap = topLevelConfigMap.get(type);
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
        CaseInsensitiveMap<TopLevelConfig> typeMap = topLevelConfigMap.get(type);
        if (typeMap == null) {
            return Collections.EMPTY_LIST;
        }

        //Перекладываем в другой контейнер, для возможности сериализации
        List<T> result = new ArrayList<T>();
        result.addAll((Collection<T>) typeMap.values());

        return kryoCloner.cloneObject(result, ArrayList.class);
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
        FieldConfig result = fieldConfigMap.get(fieldConfigKey);

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
        CollectionColumnConfig collectionColumnConfig = collectionColumnConfigMap.get(collectionColumnConfigKey);
        return kryoCloner.cloneObject(collectionColumnConfig, collectionColumnConfig.getClass());
    }

    private void initConfigurationMaps() {
        if (configuration == null) {
            throw new FatalException("Failed to initialize ConfigurationExplorerImpl because " +
                    "Configuration is null");
        }

        topLevelConfigMap.clear();
        fieldConfigMap.clear();
        attachmentDomainObjectTypes.clear();
        readPermittedToEverybodyMap.clear();
        List<DomainObjectTypeConfig> attachmentOwnerDots = new ArrayList<>();
        for (TopLevelConfig config : configuration.getConfigurationList()) {

            if (GLOBAL_SETTINGS_CLASS_NAME.equalsIgnoreCase(config.getClass().getCanonicalName())) {
                globalSettings = (GlobalSettingsConfig) config;
            }
            fillTopLevelConfigMap(config);

            if (DomainObjectTypeConfig.class.equals(config.getClass())) {
                DomainObjectTypeConfig domainObjectTypeConfig = (DomainObjectTypeConfig) config;
                fillFieldsConfigMap(domainObjectTypeConfig);
                if (domainObjectTypeConfig.getAttachmentTypesConfig() != null) {
                    attachmentOwnerDots.add(domainObjectTypeConfig);
                }
            } else if (CollectionViewConfig.class.equals(config.getClass())) {
                CollectionViewConfig collectionViewConfig = (CollectionViewConfig) config;
                fillCollectionColumnConfigMap(collectionViewConfig);
            } else if (AccessMatrixConfig.class.equals(config.getClass())) {
                AccessMatrixConfig accessMatrixConfig = (AccessMatrixConfig) config;
                fillReadPermittedToEverybodyMap(accessMatrixConfig);
            }
        }

        initConfigurationMapsOfAttachmentDomainObjectTypes(attachmentOwnerDots);
    }

    private void fillReadPermittedToEverybodyMap(AccessMatrixConfig accessMatrixConfig) {
        for (AccessMatrixStatusConfig accessMatrixStatus : accessMatrixConfig.getStatus()) {
            if (ALL_STATUSES_SIGN.equals(accessMatrixStatus.getName())) {
                for (BaseOperationPermitConfig permission : accessMatrixStatus.getPermissions()) {

                    if (ReadConfig.class.equals(permission.getClass())
                            && ((ReadConfig) permission).isPermitEverybody()) {
                        readPermittedToEverybodyMap.put(accessMatrixConfig.getType(), true);
                        return;
                    }
                }
            }
            readPermittedToEverybodyMap.put(accessMatrixConfig.getType(), false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType) {
        List<DynamicGroupConfig> dynamicGroups = new ArrayList<DynamicGroupConfig>();

        CaseInsensitiveMap<TopLevelConfig> dynamicGroupMap = topLevelConfigMap.get(DynamicGroupConfig.class);

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

        CaseInsensitiveMap<TopLevelConfig> dynamicGroupMap = topLevelConfigMap.get(DynamicGroupConfig.class);

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
        //Получение конфигурации матрицы
        AccessMatrixConfig accessMatrixConfig = getConfig(AccessMatrixConfig.class, domainObjectType);
        if (accessMatrixConfig == null) {
            return null;
        }

        //Получаем все статусы
        for (AccessMatrixStatusConfig accessStatusConfig : accessMatrixConfig.getStatus()) {
            if (status != null && status.equals(accessStatusConfig.getName())) {
                return kryoCloner.cloneObject(accessStatusConfig, accessStatusConfig.getClass());
            }
        }

        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttachmentType(String domainObjectType) {
        return attachmentDomainObjectTypes.containsKey(domainObjectType);
    }

    private void fillTopLevelConfigMap(TopLevelConfig config) {
        CaseInsensitiveMap<TopLevelConfig> typeMap = topLevelConfigMap.get(config.getClass());
        if (typeMap == null) {
            typeMap = new CaseInsensitiveMap<>();
            topLevelConfigMap.put(config.getClass(), typeMap);
        }
        typeMap.put(config.getName(), config);
    }

    private void fillSystemFields(DomainObjectTypeConfig domainObjectTypeConfig) {
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getSystemFieldConfigs()) {
            FieldConfigKey fieldConfigKey =
                    new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
            if (GenericDomainObject.STATUS_DO.equals(domainObjectTypeConfig.getName())
                    && GenericDomainObject.STATUS_FIELD_NAME.equals(fieldConfig.getName())) {
                continue;
            }
            fieldConfigMap.put(fieldConfigKey, fieldConfig);
        }
    }

    private void fillCollectionColumnConfigMap(CollectionViewConfig collectionViewConfig) {
        if (collectionViewConfig.getCollectionDisplayConfig() != null) {
            for (CollectionColumnConfig columnConfig : collectionViewConfig.getCollectionDisplayConfig().
                    getColumnConfig()) {
                FieldConfigKey fieldConfigKey =
                        new FieldConfigKey(collectionViewConfig.getName(), columnConfig.getField());
                collectionColumnConfigMap.put(fieldConfigKey, columnConfig);

            }
        }
    }

    private void fillFieldsConfigMap(DomainObjectTypeConfig domainObjectTypeConfig) {
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            FieldConfigKey fieldConfigKey =
                    new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
            fieldConfigMap.put(fieldConfigKey, fieldConfig);
        }
        fillSystemFields(domainObjectTypeConfig);
    }

    private void initConfigurationMapsOfAttachmentDomainObjectTypes(List<DomainObjectTypeConfig> ownerAttachmentDOTs) {
        if (ownerAttachmentDOTs == null || ownerAttachmentDOTs.isEmpty()) {
            return;
        }

        try {
            AttachmentPrototypeHelper factory = new AttachmentPrototypeHelper();
            for (DomainObjectTypeConfig domainObjectTypeConfig : ownerAttachmentDOTs) {
                for (AttachmentTypeConfig attachmentTypeConfig : domainObjectTypeConfig.getAttachmentTypesConfig()
                        .getAttachmentTypeConfigs()) {
                    DomainObjectTypeConfig attachmentDomainObjectTypeConfig =
                            factory.makeAttachmentConfig(attachmentTypeConfig.getName(),
                                    domainObjectTypeConfig.getName());
                    fillTopLevelConfigMap(attachmentDomainObjectTypeConfig);
                    fillFieldsConfigMap(attachmentDomainObjectTypeConfig);
                    attachmentDomainObjectTypes.put(attachmentDomainObjectTypeConfig.getName(), attachmentDomainObjectTypeConfig.getName());
                }
            }
        } catch (IOException e) {
            throw new ConfigurationException(e);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
    }

    private class FieldConfigKey {

        private String domainObjectName;
        private String fieldConfigName;

        private FieldConfigKey(String domainObjectName, String fieldConfigName) {
            this.domainObjectName = domainObjectName.toLowerCase();
            this.fieldConfigName = fieldConfigName.toLowerCase();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FieldConfigKey that = (FieldConfigKey) o;

            if (domainObjectName != null ? !domainObjectName.equals(that.domainObjectName) :
                    that.domainObjectName != null) {
                return false;
            }
            if (fieldConfigName != null ? !fieldConfigName.equals(that.fieldConfigName) :
                    that.fieldConfigName != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = domainObjectName != null ? domainObjectName.hashCode() : 0;
            result = 31 * result + (fieldConfigName != null ? fieldConfigName.hashCode() : 0);
            return result;
        }
    }

    private class TopLevelConfigKey {
        private String key;

        private TopLevelConfigKey(String key) {
            if (key != null) {
                this.key = key.toLowerCase();
            } else {
                this.key = null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TopLevelConfigKey that = (TopLevelConfigKey) o;

            if (key != null ? !key.equals(that.key) : that.key != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }
    }

    private class PrototypeHelper {
        private ByteArrayInputStream bis;

        private PrototypeHelper(String templateName) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            TopLevelConfig templateDomainObjectTypeConfig = getConfig(DomainObjectTypeConfig.class, templateName);
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

    public boolean isReadPermittedToEverybody(String domainObjectType) {
        if (readPermittedToEverybodyMap.get(domainObjectType) != null) {
            return readPermittedToEverybodyMap.get(domainObjectType);
        }
        return false;
    }
}
