package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.ArrayList;
import java.util.HashMap;

import ru.intertrust.cm.core.config.AccessMatrixConfig;
import ru.intertrust.cm.core.config.DomainObjectFieldsConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

public class FakeConfigurationExplorer extends StubConfigurationExplorer {

    public static class TypeConfigBuilder {
        private DomainObjectTypeConfig typeConfig = new DomainObjectTypeConfig();
        private AccessMatrixConfig matrixConfig;

        public TypeConfigBuilder(String name) {
            typeConfig.setName(name);
            DomainObjectFieldsConfig fieldsConfig = new DomainObjectFieldsConfig();
            fieldsConfig.setFieldConfigs(new ArrayList<FieldConfig>());
            typeConfig.setDomainObjectFieldsConfig(fieldsConfig);
        }

        public TypeConfigBuilder parent(String typeName) {
            typeConfig.setExtendsAttribute(typeName);
            return this;
        }

        public TypeConfigBuilder linkedTo(String typeName, String fieldName) {
            if (matrixConfig == null) {
                matrixConfig = new AccessMatrixConfig();
                matrixConfig.setType(typeConfig.getName());
            }
            ReferenceFieldConfig parentLink = new ReferenceFieldConfig();
            parentLink.setName(fieldName);
            parentLink.setType(typeName);
            typeConfig.getFieldConfigs().add(parentLink);
            matrixConfig.setMatrixReference(fieldName);
            return this;
        }

        public DomainObjectTypeConfig getTypeConfig() {
            return typeConfig;
        }

        public AccessMatrixConfig getMatrixConfig() {
            return matrixConfig;
        }

    }

    private HashMap<Class<?>, HashMap<String, TopLevelConfig>> categoriesMap =
            new HashMap<Class<?>, HashMap<String, TopLevelConfig>>();

    public void addConfig(TopLevelConfig config) {
        if (!categoriesMap.containsKey(config.getClass())) {
            categoriesMap.put(config.getClass(), new HashMap<String, TopLevelConfig>());
        }
        HashMap<String, TopLevelConfig> subcategoryMap = categoriesMap.get(config.getClass());
        subcategoryMap.put(config.getName(), config);
    }

    @Override
    public <T> T getConfig(Class<T> type, String name) {
        HashMap<String, TopLevelConfig> category = categoriesMap.get(type);
        if (category == null) {
            return null;
        } else {
            return type.cast(category.get(name));
        }
    }

    @Override
    public String getMatrixReferenceTypeName(String childTypeName) {
        HashMap<String, TopLevelConfig> matrices = categoriesMap.get(AccessMatrixConfig.class);
        if (matrices == null) {
            return null;
        } else {
            AccessMatrixConfig matrixConfig = (AccessMatrixConfig) matrices.get(childTypeName);
            if (matrixConfig == null) {
                String parentType = getConfig(DomainObjectTypeConfig.class, childTypeName).getExtendsAttribute();
                if (parentType != null) {
                    return getMatrixReferenceTypeName(parentType);
                } else {
                    return null;
                }
            } else {
                String fieldName = matrixConfig.getMatrixReference();
                DomainObjectTypeConfig typeConfig = (DomainObjectTypeConfig) categoriesMap.get(DomainObjectTypeConfig.class).get(childTypeName);
                for (FieldConfig fieldConfig : typeConfig.getFieldConfigs()) {
                    if (fieldConfig.getName().equals(fieldName) && fieldConfig instanceof ReferenceFieldConfig) {
                        return ((ReferenceFieldConfig) fieldConfig).getType();
                    }
                }
                return null;
            }
        }
    }

    @Override
    public AccessMatrixConfig getAccessMatrixByObjectType(String domainObjectType) {
        return getConfig(AccessMatrixConfig.class, domainObjectType);
    }

    @Override
    public String getDomainObjectRootType(String typeName) {
        DomainObjectTypeConfig typeConfig = (DomainObjectTypeConfig) categoriesMap.get(DomainObjectTypeConfig.class).get(typeName);
        String parentTypeName = typeConfig.getExtendsAttribute();
        if (parentTypeName == null) {
            return typeName;
        } else {
            return getDomainObjectRootType(parentTypeName);
        }
    }

    public void createTypeConfig(TypeConfigBuilder builder) {
        addConfig(builder.getTypeConfig());
        if (builder.getMatrixConfig() != null) {
            addConfig(builder.getMatrixConfig());
        }
    }
}