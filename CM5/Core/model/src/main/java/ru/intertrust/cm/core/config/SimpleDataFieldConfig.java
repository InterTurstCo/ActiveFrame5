package ru.intertrust.cm.core.config;


import org.simpleframework.xml.Attribute;

import java.util.Objects;

public class SimpleDataFieldConfig {
    @Attribute(required = true)
    private String name;

    @Attribute(required = true)
    private SimpleDataFieldType type;

    @Attribute(required = true)
    private boolean index;

    @Attribute(required = true)
    private boolean storage;

    @Attribute(required = false)
    private Boolean multivalue;

    public SimpleDataFieldConfig() {
    }

    public SimpleDataFieldConfig(String name, SimpleDataFieldType type, boolean index, boolean storage, boolean multivalue) {
        this.name = name;
        this.type = type;
        this.index = index;
        this.storage = storage;
        this.multivalue = multivalue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SimpleDataFieldType getType() {
        return type;
    }

    public void setType(SimpleDataFieldType type) {
        this.type = type;
    }

    public boolean isIndex() {
        return index;
    }

    public void setIndex(boolean index) {
        this.index = index;
    }

    public boolean isStorage() {
        return storage;
    }

    public void setStorage(boolean storage) {
        this.storage = storage;
    }

    public Boolean isMultivalue() {
        return multivalue;
    }

    public void setMultivalue(Boolean multivalue) {
        this.multivalue = multivalue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleDataFieldConfig that = (SimpleDataFieldConfig) o;
        return index == that.index &&
                storage == that.storage &&
                Objects.equals(name, that.name) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, index, storage);
    }
}
