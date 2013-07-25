package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.*;
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

    private Configuration configuration;

    private Map<Class, Map<String, TopLevelConfig>> topLevelConfigMap = new HashMap<>();
    private Map<FieldConfigKey, FieldConfig> fieldConfigMap = new HashMap<>();

    /**
     * Создает {@link ConfigurationExplorerImpl}
     */
    public ConfigurationExplorerImpl() {
    }

    /**
     * Создает {@link ConfigurationExplorerImpl}
     */
    public ConfigurationExplorerImpl(Configuration configuration) {
        this.configuration = configuration;
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
    @Override
    public void build() {
        initConfigurationMaps();

        ConfigurationLogicalValidator logicalValidator = new ConfigurationLogicalValidator(this);
        logicalValidator.validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    public <T> Collection<T> getConfigs(Class<T> type) {
        Map<String, TopLevelConfig> typeMap = topLevelConfigMap.get(type);
        if(typeMap == null) {
            return Collections.EMPTY_LIST;
        }

        return (Collection<T>) typeMap.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        FieldConfigKey fieldConfigKey = new FieldConfigKey(domainObjectConfigName, fieldConfigName);
        return fieldConfigMap.get(fieldConfigKey);
    }

    private void initConfigurationMaps() {
        if(configuration == null) {
            throw new FatalException("Failed to initialize ConfigurationExplorerImpl because " +
                    "Configuration is null");
        }

        topLevelConfigMap.clear();
        fieldConfigMap.clear();

        List<DomainObjectTypeConfig> ownerAttachmentDOTs = new ArrayList<>();
        for (TopLevelConfig config : configuration.getConfigurationList()) {
            fillTopLevelConfigMap(config);

            if (DomainObjectTypeConfig.class.equals(config.getClass())) {
                DomainObjectTypeConfig domainObjectTypeConfig = (DomainObjectTypeConfig) config;
                fillFieldsInFieldsConfigMap(domainObjectTypeConfig);
                if (domainObjectTypeConfig.getAttachmentTypesConfig() != null) {
                    ownerAttachmentDOTs.add(domainObjectTypeConfig);
                }
            }
        }

        initConfigurationMapsOfAttachmentDomainObjectTypes(ownerAttachmentDOTs);
    }

    private void fillTopLevelConfigMap(TopLevelConfig config) {
        Map<String, TopLevelConfig> typeMap = topLevelConfigMap.get(config.getClass());
        if(typeMap == null) {
            typeMap = new HashMap<>();
            topLevelConfigMap.put(config.getClass(), typeMap);
        }
        typeMap.put(config.getName(), config);
    }

    private void fillFieldsInFieldsConfigMap(DomainObjectTypeConfig domainObjectTypeConfig) {
        for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
            FieldConfigKey fieldConfigKey =
                    new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
            fieldConfigMap.put(fieldConfigKey, fieldConfig);
        }
    }

    private void initConfigurationMapsOfAttachmentDomainObjectTypes(List<DomainObjectTypeConfig> ownerAttachmentDOTs) {
        try {
            PrototypeHelper factory = new PrototypeHelper("Attachment");
            for (DomainObjectTypeConfig domainObjectTypeConfig : ownerAttachmentDOTs) {
                for (AttachmentTypeConfig attachmentTypeConfig : domainObjectTypeConfig.getAttachmentTypesConfig().getAttachmentTypeConfigs()) {
                    DomainObjectTypeConfig attachmentDomainObjectTypeConfig = factory.makeDomainObjectTypeConfig(attachmentTypeConfig.getName(), domainObjectTypeConfig.getName());
                    fillTopLevelConfigMap(attachmentDomainObjectTypeConfig);
                    fillFieldsInFieldsConfigMap(attachmentDomainObjectTypeConfig);
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
            this.fieldConfigName = fieldConfigName;
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

            if (domainObjectName != null ? !domainObjectName.equals(that.domainObjectName) : that.domainObjectName != null) {
                return false;
            }
            if (fieldConfigName != null ? !fieldConfigName.equals(that.fieldConfigName) : that.fieldConfigName != null) {
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
            TopLevelConfig templateDomainObjectTypeConfig = topLevelConfigMap.get(DomainObjectTypeConfig.class).get(templateName);
            oos.writeObject(templateDomainObjectTypeConfig);
            oos.close();
            bis = new ByteArrayInputStream(bos.toByteArray());
        }

        public DomainObjectTypeConfig makeDomainObjectTypeConfig(String name, String parentName)
                throws IOException, ClassNotFoundException {
            bis.reset();
            DomainObjectTypeConfig cloneDomainObjectTypeConfig = (DomainObjectTypeConfig) new ObjectInputStream(bis).readObject();
            cloneDomainObjectTypeConfig.setTemplate(false);
            DomainObjectTypeConfig parentDomainObjectTypeConfig = (DomainObjectTypeConfig) topLevelConfigMap.get(DomainObjectTypeConfig.class).get(parentName);
            DomainObjectParentConfig parentConfig = new DomainObjectParentConfig();
            parentConfig.setName(parentDomainObjectTypeConfig.getName());
            cloneDomainObjectTypeConfig.setParentConfig(parentConfig);
            cloneDomainObjectTypeConfig.setName(name);
            return cloneDomainObjectTypeConfig;
        }
    }

}
