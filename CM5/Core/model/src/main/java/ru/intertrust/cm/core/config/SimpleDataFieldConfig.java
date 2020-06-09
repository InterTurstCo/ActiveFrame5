package ru.intertrust.cm.core.config;


import org.simpleframework.xml.Attribute;

public class SimpleDataFieldConfig {
    @Attribute(required = true)
    private String name;

    @Attribute(required = true)
    private SimpleDataFieldType type;

    @Attribute(required = true)
    private boolean index;

    @Attribute(required = true)
    private boolean storage;

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
}
