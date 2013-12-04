package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.config.DoelAware;

public class IndexedFieldConfig extends DoelAware {

    @Attribute(required = true)
    private String name;

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && name.equals(((IndexedFieldConfig) obj).name);
    }
}
