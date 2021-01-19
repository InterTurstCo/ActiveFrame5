package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveHashMap;
import ru.intertrust.cm.core.config.AccessMatrixConfig;
import ru.intertrust.cm.core.config.DomainObjectFieldsConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.LongFieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.StringFieldConfig;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

public class FakeConfigurationExplorer extends StubConfigurationExplorer {

    public static class TypeConfigBuilder {
        private DomainObjectTypeConfig typeConfig = new DomainObjectTypeConfig();
        private AccessMatrixConfig matrixConfig;

        public TypeConfigBuilder(String name, boolean hasAccessMatrix) {
            typeConfig.setName(name);
            DomainObjectFieldsConfig fieldsConfig = new DomainObjectFieldsConfig();
            fieldsConfig.setFieldConfigs(new ArrayList<FieldConfig>());
            typeConfig.setDomainObjectFieldsConfig(fieldsConfig);
            if (hasAccessMatrix) {
                matrixConfig = new AccessMatrixConfig();
                matrixConfig.setType(typeConfig.getName());
                matrixConfig.setModuleName("test");
            }
        }

        public TypeConfigBuilder(String name) {
            this(name, true);
        }

        public TypeConfigBuilder parent(String typeName) {
            typeConfig.setExtendsAttribute(typeName);
            return this;
        }

        public TypeConfigBuilder linkedTo(String typeName, String fieldName) {

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

        public TypeConfigBuilder addLongField(String field) {
            FieldConfig config = new LongFieldConfig();
            config.setName(field);
            typeConfig.getFieldConfigs().add(config);
            return this;
        }

        public TypeConfigBuilder addStringField(String field) {
            FieldConfig config = new StringFieldConfig();
            config.setName(field);
            typeConfig.getFieldConfigs().add(config);
            return this;
        }

        public TypeConfigBuilder addReferenceField(String field, String type) {
            ReferenceFieldConfig config = new ReferenceFieldConfig();
            config.setName(field);
            config.setType(type);
            typeConfig.getFieldConfigs().add(config);
            return this;
        }

    }

    private HashMap<Class<?>, CaseInsensitiveHashMap<TopLevelConfig>> categoriesMap =
            new HashMap<Class<?>, CaseInsensitiveHashMap<TopLevelConfig>>();

    public void addConfig(TopLevelConfig config) {
        if (!categoriesMap.containsKey(config.getClass())) {
            categoriesMap.put(config.getClass(), new CaseInsensitiveHashMap<TopLevelConfig>());
        }
        CaseInsensitiveHashMap<TopLevelConfig> subcategoryMap = categoriesMap.get(config.getClass());
        subcategoryMap.put(config.getName(), config);
    }

    @Override
    public <T> T getConfig(Class<T> type, String name) {
        CaseInsensitiveHashMap<TopLevelConfig> category = categoriesMap.get(type);
        if (category == null) {
            return null;
        } else {
            return type.cast(category.get(name));
        }
    }

    @Override
    public FieldConfig getFieldConfig(String domainObjectConfigName, String fieldConfigName) {
        if (domainObjectConfigName == null || fieldConfigName == null) {
            return null;
        }
        DomainObjectTypeConfig c = getConfig(DomainObjectTypeConfig.class, domainObjectConfigName);
        if (c == null) {
            return null;
        }
        for (FieldConfig fc : c.getFieldConfigs()) {
            if (fc.getName().equalsIgnoreCase(fieldConfigName)) {
                return fc;
            }
        }
        return null;
    }

    @Override
    public DomainObjectTypeConfig getDomainObjectTypeConfig(String typeName) {
        return getConfig(DomainObjectTypeConfig.class, typeName);
    }

    @Override
    public Collection<DomainObjectTypeConfig> findChildDomainObjectTypes(String typeName, boolean includeIndirect) {

        HashMap<String, List<DomainObjectTypeConfig>> childrenMap = new HashMap<String, List<DomainObjectTypeConfig>>();
        for (TopLevelConfig c : getCategorySafe(DomainObjectTypeConfig.class).values()) {
            DomainObjectTypeConfig type = (DomainObjectTypeConfig) c;
            String parent = type.getExtendsAttribute();
            if (parent != null) {
                if (!childrenMap.containsKey(parent)) {
                    childrenMap.put(parent, new ArrayList<DomainObjectTypeConfig>());
                }
                childrenMap.get(parent).add(type);
            }
        }

        ArrayList<DomainObjectTypeConfig> children = new ArrayList<>();
        if (childrenMap.containsKey(typeName)) {
            children.addAll(childrenMap.get(typeName));
        }
        if (includeIndirect) {
            ConcurrentLinkedQueue<DomainObjectTypeConfig> queue = new ConcurrentLinkedQueue<DomainObjectTypeConfig>();
            queue.addAll(children);
            for (DomainObjectTypeConfig type : queue) {
                if (childrenMap.containsKey(type.getName())) {
                    children.addAll(childrenMap.get(type.getName()));
                    queue.addAll(childrenMap.get(type.getName()));
                }
            }
        }
        return children;
    }

    private CaseInsensitiveHashMap<TopLevelConfig> getCategorySafe(Class<? extends TopLevelConfig> c) {
        if (!categoriesMap.containsKey(c)) {
            categoriesMap.put(c, new CaseInsensitiveHashMap<TopLevelConfig>());
        }
        return categoriesMap.get(c);
    }

    @Override
    public String getMatrixReferenceTypeName(String childTypeName) {
        CaseInsensitiveHashMap<TopLevelConfig> matrices = getCategorySafe(AccessMatrixConfig.class);
        if (matrices == null) {
            return null;
        } else {
            AccessMatrixConfig matrixConfig = (AccessMatrixConfig) matrices.get("test:" + childTypeName);
            if (matrixConfig == null) {
                String parentType = getConfig(DomainObjectTypeConfig.class, childTypeName).getExtendsAttribute();
                if (parentType != null) {
                    return getMatrixReferenceTypeName(parentType);
                } else {
                    return null;
                }
            } else {
                String fieldName = matrixConfig.getMatrixReference();
                if (fieldName == null) {
                    return childTypeName;
                } else {
                    DomainObjectTypeConfig typeConfig = (DomainObjectTypeConfig) getCategorySafe(DomainObjectTypeConfig.class).get(childTypeName);
                    for (FieldConfig fieldConfig : typeConfig.getFieldConfigs()) {
                        if (fieldConfig.getName().equals(fieldName) && fieldConfig instanceof ReferenceFieldConfig) {
                            String nextType = ((ReferenceFieldConfig) fieldConfig).getType();
                            return getMatrixReferenceTypeName(nextType);
                        }
                    }
                    return null;
                }
            }
        }
    }

    @Override
    public AccessMatrixConfig getAccessMatrixByObjectType(String domainObjectType) {
        return getConfig(AccessMatrixConfig.class, "test:"+domainObjectType);
    }

    @Override
    public String getDomainObjectRootType(String typeName) {
        DomainObjectTypeConfig typeConfig = (DomainObjectTypeConfig) getCategorySafe(DomainObjectTypeConfig.class).get(typeName);
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