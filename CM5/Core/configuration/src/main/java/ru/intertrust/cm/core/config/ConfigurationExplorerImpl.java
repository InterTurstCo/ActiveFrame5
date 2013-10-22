package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.model.*;
import ru.intertrust.cm.core.config.model.base.Configuration;
import ru.intertrust.cm.core.config.model.base.TopLevelConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionColumnConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionViewConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.model.FatalException;

import java.io.*;
import java.util.*;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 * После создания объекта данного класса требуется выполнить инициализацию через вызов метода {@link #build()}.
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 5:21 PM
 */
public class ConfigurationExplorerImpl implements ConfigurationExplorer {
    private final static Logger logger = LoggerFactory.getLogger(ConfigurationExplorerImpl.class);

    private Configuration configuration;

    private Map<Class<?>, Map<String, TopLevelConfig>> topLevelConfigMap = new HashMap<>();
    private Map<FieldConfigKey, FieldConfig> fieldConfigMap = new HashMap<>();
    private Map<FieldConfigKey, CollectionColumnConfig> collectionColumnConfigMap = new HashMap<>();
    private Map<String, LinkConfig> linkConfigMap = new HashMap<>();

    @Autowired
    FormLogicalValidator formLogicalValidator;

    @Autowired
    NavigationPanelLogicalValidator navigationPanelLogicalValidator;

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
        return configuration;
    }

    /**
     * Устанавливает конфигурацию
     * @param configuration конфигурация
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    public void build() {
        initConfigurationMaps();
    }

    /**
     * Каждый логический валидатор находится в блоке try/catch
     * для отображения всех ошибок, возникнувших в результате
     * валидации, а не только первого бросившего exception
     *
     */
    private void validate() {
        DomainObjectLogicalValidator domainObjectLogicalValidator = new DomainObjectLogicalValidator(this);
        domainObjectLogicalValidator.validate();

    }
    public void validateGui() {
        try {
            navigationPanelLogicalValidator.setConfigurationExplorer(this);
            navigationPanelLogicalValidator.validate();
        } catch (ConfigurationException e) {

        }
        try {
            formLogicalValidator.setConfigurationExplorer(this);
            formLogicalValidator.validate();
        } catch (ConfigurationException e) {

        }

        try {
            CollectionViewLogicalValidator collectionLogicalValidator = new CollectionViewLogicalValidator(this);
            collectionLogicalValidator.validate();
        } catch (ConfigurationException e) {

        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfig(Class<T> type, String name) {
        Map<String, TopLevelConfig> typeMap = topLevelConfigMap.get(type);
        if(typeMap == null) {
            return null;
        }

        return (T) typeMap.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getConfigs(Class<T> type) {
        Map<String, TopLevelConfig> typeMap = topLevelConfigMap.get(type);
        if(typeMap == null) {
            return Collections.EMPTY_LIST;
        }

        return (Collection<T>) typeMap.values();
    }

    @Override
    public Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect) {
        ArrayList<DomainObjectTypeConfig> childTypes = new ArrayList<>();
        Collection<DomainObjectTypeConfig> allTypes = getConfigs(DomainObjectTypeConfig.class);
        for (DomainObjectTypeConfig type : allTypes) {
            if (typeName.equals(type.getExtendsAttribute())) {
                childTypes.add(type);
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
            return result;
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
    public CollectionColumnConfig getCollectionColumnConfig(String collectionConfigName, String columnConfigName) {
        FieldConfigKey collectionColumnConfigKey = new FieldConfigKey(collectionConfigName, columnConfigName);
        return collectionColumnConfigMap.get(collectionColumnConfigKey);
    }


    private void initConfigurationMaps() {
        if(configuration == null) {
            throw new FatalException("Failed to initialize ConfigurationExplorerImpl because " +
                    "Configuration is null");
        }

        topLevelConfigMap.clear();
        fieldConfigMap.clear();

        List<DomainObjectTypeConfig> attachmentOwnerDots = new ArrayList<>();
        for (TopLevelConfig config : configuration.getConfigurationList()) {
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
            } else if (NavigationConfig.class.equals(config.getClass())) {
                NavigationConfig navigationConfig = (NavigationConfig) config;
                fillLinkConfigMap(navigationConfig);
            }
        }

        initConfigurationMapsOfAttachmentDomainObjectTypes(attachmentOwnerDots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DynamicGroupConfig> getDynamicGroupConfigsByContextType(String domainObjectType) {
        List<DynamicGroupConfig> dynamicGroups = new ArrayList<DynamicGroupConfig>();

        Map<String, TopLevelConfig> dynamicGroupMap = topLevelConfigMap.get(DynamicGroupConfig.class);

        for (String groupName : dynamicGroupMap.keySet()) {
            DynamicGroupConfig dynamicGroup = (DynamicGroupConfig) dynamicGroupMap.get(groupName);
            if (dynamicGroup.getContext() != null && dynamicGroup.getContext().getDomainObject() != null) {
                String objectType = dynamicGroup.getContext().getDomainObject().getType();

                if (objectType.equals(domainObjectType)) {
                    dynamicGroups.add(dynamicGroup);
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

        Map<String, TopLevelConfig> dynamicGroupMap = topLevelConfigMap.get(DynamicGroupConfig.class);

        for (String groupName : dynamicGroupMap.keySet()) {
            DynamicGroupConfig dynamicGroup = (DynamicGroupConfig) dynamicGroupMap.get(groupName);

            if (dynamicGroup.getMembers() != null && dynamicGroup.getMembers().getTrackDomainObjects() != null) {
                TrackDomainObjectsConfig trackDomainObjectsConfig = dynamicGroup.getMembers().getTrackDomainObjects();

                String configuredStatus = trackDomainObjectsConfig.getStatus();
                String configuredType = trackDomainObjectsConfig.getType();
                if (trackDOTypeName.equalsIgnoreCase(configuredType)) {

                    if (configuredStatus == null || configuredStatus.equals(status)) {
                        dynamicGroups.add(dynamicGroup);
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
    public AccessMatrixConfig getAccessMatrixByObjectTypeAndStatus(String domainObjectType, String status) {
        Map<String, TopLevelConfig> accessMatrixMap = topLevelConfigMap.get(AccessMatrixConfig.class);

        for (String accessMatrixObjectType : accessMatrixMap.keySet()) {
            AccessMatrixConfig accessMatrixConfig = (AccessMatrixConfig) accessMatrixMap.get(accessMatrixObjectType);
            String accessMatrixStatus = null;
            if (accessMatrixConfig.getStatus() != null && accessMatrixConfig.getStatus().getName() != null) {
                accessMatrixStatus = accessMatrixConfig.getStatus().getName();
            }

            if(status!= null && status.equals(accessMatrixStatus) && accessMatrixObjectType.equals(domainObjectType)){
                return accessMatrixConfig;
            }
        }

        return null;
    }

    @Override
    public ContextRoleConfig getContextRoleByName(String contextRoleName) {
        Map<String, TopLevelConfig> contextRoleMap = topLevelConfigMap.get(ContextRoleConfig.class);

        for (String roleName : contextRoleMap.keySet()) {
            if(contextRoleName.equals(roleName)){
                return (ContextRoleConfig)contextRoleMap.get(roleName);
            }
        }

        return null;
    }

    @Override
    public DynamicGroupConfig getDynamicGroupByName(String dynamicGroupName) {
        Map<String, TopLevelConfig> dynamicGroupMap = topLevelConfigMap.get(DynamicGroupConfig.class);

        for (String roleName : dynamicGroupMap.keySet()) {
            if (dynamicGroupName.equals(roleName)) {
                return (DynamicGroupConfig) dynamicGroupMap.get(roleName);
            }
        }

        return null;
    }

    private void fillTopLevelConfigMap(TopLevelConfig config) {
        Map<String, TopLevelConfig> typeMap = topLevelConfigMap.get(config.getClass());
        if(typeMap == null) {
            typeMap = new HashMap<>();
            topLevelConfigMap.put(config.getClass(), typeMap);
        }
        typeMap.put(config.getName(), config);
    }

    private void fillLinkConfigMap(NavigationConfig navigationPanel) {
        for (LinkConfig linkConfig : navigationPanel.getLinkConfigList()) {
            linkConfigMap.put(linkConfig.getName(), linkConfig);
        }
    }

    private void fillSystemFields(DomainObjectTypeConfig domainObjectTypeConfig) {
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getSystemFieldConfigs()) {
            FieldConfigKey fieldConfigKey =
                    new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
            fieldConfigMap.put(fieldConfigKey, fieldConfig);
        }
    }

    private void fillCollectionColumnConfigMap(CollectionViewConfig collectionViewConfig) {
     if (collectionViewConfig.getCollectionDisplayConfig() != null) {
            for (CollectionColumnConfig columnConfig : collectionViewConfig.getCollectionDisplayConfig().
                    getColumnConfig()) {
                        FieldConfigKey fieldConfigKey =
                        new FieldConfigKey(collectionViewConfig.getCollection(), columnConfig.getField());
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
            this.domainObjectName = domainObjectName;
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

            if (domainObjectName != null ? !domainObjectName.equalsIgnoreCase(that.domainObjectName) :
                    that.domainObjectName != null) {
                return false;
            }
            if (fieldConfigName != null ? !fieldConfigName.equalsIgnoreCase(that.fieldConfigName) :
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

    private class PrototypeHelper {
        private ByteArrayInputStream bis;

        private PrototypeHelper(String templateName) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            topLevelConfigMap.get(DomainObjectTypeConfig.class).get(templateName);
            TopLevelConfig templateDomainObjectTypeConfig =
                    topLevelConfigMap.get(DomainObjectTypeConfig.class).get(templateName);
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
