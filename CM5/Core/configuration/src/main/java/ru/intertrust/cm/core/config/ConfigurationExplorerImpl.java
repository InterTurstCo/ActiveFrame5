package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.AccessMatrixConfig;
import ru.intertrust.cm.core.config.model.CollectionConfig;
import ru.intertrust.cm.core.config.model.Configuration;
import ru.intertrust.cm.core.config.model.ContextRoleConfig;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.model.DynamicGroupConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.StaticGroupConfig;
import ru.intertrust.cm.core.model.FatalException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Предоставляет быстрый доступ к элементам конфигурации.
 * После создания объекта данного класса требуется выполнить инициализацию через вызов метода {@link #build()}.
 * @author vmatsukevich
 *         Date: 6/12/13
 *         Time: 5:21 PM
 */
public class ConfigurationExplorerImpl implements ConfigurationExplorer {

    private Configuration configuration;

    private Map<String, DomainObjectTypeConfig> domainObjectConfigMap = new HashMap<>();
    private Map<String, CollectionConfig> collectionConfigMap = new HashMap<>();
    private Map<FieldConfigKey, FieldConfig> fieldConfigMap = new HashMap<>();
    
    private AccessConfig accessConfig = new AccessConfig();

    public ConfigurationExplorerImpl() {
    }

    public ConfigurationExplorerImpl(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Возвращает конфигурацию
     * @return конфигурация
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
     * Выполняет инициализацию объекта данного класса: создает внутреннюю структуру,
     * обеспечивающую быстрый доступ к элементам конфигурации и выполняет логическую валидацию конфигурации
     */
    @Override
    public void build() {
        initConfigurationMaps();

        ConfigurationLogicalValidator logicalValidator = new ConfigurationLogicalValidator(this);
        logicalValidator.validate();
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.config.ConfigurationExplorer#getDomainObjectConfigs()}
     */
    @Override
    public Collection<DomainObjectTypeConfig> getDomainObjectConfigs() {
        return domainObjectConfigMap.values();
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.config.ConfigurationExplorer#getCollectionConfigs()}
     */
    @Override
    public Collection<CollectionConfig> getCollectionConfigs() {
        return collectionConfigMap.values();
    }

    /**
     * Смотри {@link ConfigurationExplorer#getDomainObjectTypeConfig(String)}
     */
    @Override
    public DomainObjectTypeConfig getDomainObjectTypeConfig(String name) {
        return domainObjectConfigMap.get(name);
    }

    /**
     * Смотри {@link ConfigurationExplorer#getCollectionConfig(String)}
     */
    @Override
    public CollectionConfig getCollectionConfig(String name) {
        return collectionConfigMap.get(name);
    }

    /**
     * Смотри {@link ConfigurationExplorer#getStaticGroupConfig(String)}
     */
    @Override
    public StaticGroupConfig getStaticGroupConfig(String name) {
        return accessConfig.getStaticGroupConfigMap().get(name);
    }
    
    /**
     * Смотри {@link ConfigurationExplorer#getDynamicGroupConfig(String)}
     */
    @Override
    public DynamicGroupConfig getDynamicGroupConfig(String name) {
        return accessConfig.getDynamicGroupConfigMap().get(name);
    }

    /**
     * Смотри {@link ConfigurationExplorer#getContextRoleConfig(String)}
     */
    @Override
    public ContextRoleConfig getContextRoleConfig(String name) {
        return accessConfig.getContextRoleConfigMap().get(name);
    }

    /**
     * Смотри {@link ConfigurationExplorer#getAccessMatrixConfig(String)}
     */
    @Override
    public AccessMatrixConfig getAccessMatrixConfig(String domainObjectType) {
        return accessConfig.getAccessMatrixConfigMap().get(domainObjectType);
    }
    
    /**
     * Смотри {@link ConfigurationExplorer#getFieldConfig(String, String)}
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

        domainObjectConfigMap.clear();
        collectionConfigMap.clear();
        fieldConfigMap.clear();
        accessConfig.clear();                
        
        for (Object config : configuration.getConfigurationList()) {
            if (DomainObjectTypeConfig.class.equals(config.getClass())) {
                DomainObjectTypeConfig domainObjectTypeConfig = (DomainObjectTypeConfig) config;
                domainObjectConfigMap.put(domainObjectTypeConfig.getName(), domainObjectTypeConfig);

                for (FieldConfig fieldConfig : domainObjectTypeConfig.getFieldConfigs()) {
                    FieldConfigKey fieldConfigKey = new FieldConfigKey(domainObjectTypeConfig.getName(), fieldConfig.getName());
                    fieldConfigMap.put(fieldConfigKey, fieldConfig);
                }
            } else if(CollectionConfig.class.equals(config.getClass())) {
                CollectionConfig collectionConfig = (CollectionConfig) config;
                collectionConfigMap.put(collectionConfig.getName(), collectionConfig);
            } else if (StaticGroupConfig.class.equals(config.getClass())) {
                StaticGroupConfig staticGroupConfig = (StaticGroupConfig) config;
                accessConfig.getStaticGroupConfigMap().put(staticGroupConfig.getName(), staticGroupConfig);
            } else if (DynamicGroupConfig.class.equals(config.getClass())) {
                DynamicGroupConfig dynamicGroupConfig = (DynamicGroupConfig) config;
                accessConfig.getDynamicGroupConfigMap().put(dynamicGroupConfig.getName(), dynamicGroupConfig);
            } else if (ContextRoleConfig.class.equals(config.getClass())) {
                ContextRoleConfig contextRoleConfig = (ContextRoleConfig) config;
                accessConfig.getContextRoleConfigMap().put(contextRoleConfig.getName(), contextRoleConfig);
            } else if (AccessMatrixConfig.class.equals(config.getClass())) {
                AccessMatrixConfig accessMatrixConfig = (AccessMatrixConfig) config;
                accessConfig.getAccessMatrixConfigMap().put(accessMatrixConfig.getType(), accessMatrixConfig);
            } else {
                //throw new ConfigurationException("Unknown configuration type '" + config.getClass() + "'");
            }
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FieldConfigKey that = (FieldConfigKey) o;

            if (domainObjectName != null ? !domainObjectName.equals(that.domainObjectName) : that.domainObjectName != null)
                return false;
            if (fieldConfigName != null ? !fieldConfigName.equals(that.fieldConfigName) : that.fieldConfigName != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = domainObjectName != null ? domainObjectName.hashCode() : 0;
            result = 31 * result + (fieldConfigName != null ? fieldConfigName.hashCode() : 0);
            return result;
        }

    }
    
    /**
     * Обертывает кеширование конфигурации прав доступа.
     * @author atsvetkov
     */
    private class AccessConfig {

        private Map<String, StaticGroupConfig> staticGroupConfigMap = new HashMap<>();
        private Map<String, DynamicGroupConfig> dynamicGroupConfigMap = new HashMap<>();
        private Map<String, ContextRoleConfig> contextRoleConfigMap = new HashMap<>();
        private Map<String, AccessMatrixConfig> accessMatrixConfigMap = new HashMap<>();

        public Map<String, StaticGroupConfig> getStaticGroupConfigMap() {
            return staticGroupConfigMap;
        }

        public Map<String, DynamicGroupConfig> getDynamicGroupConfigMap() {
            return dynamicGroupConfigMap;
        }

        public Map<String, ContextRoleConfig> getContextRoleConfigMap() {
            return contextRoleConfigMap;
        }

        public Map<String, AccessMatrixConfig> getAccessMatrixConfigMap() {
            return accessMatrixConfigMap;
        }

        public void clear() {
            staticGroupConfigMap.clear();
            dynamicGroupConfigMap.clear();
            contextRoleConfigMap.clear();
            accessMatrixConfigMap.clear();
        }

    }
   
}
