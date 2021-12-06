package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Root(name = "simple-data")
public class SimpleDataConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @ElementList(name = "fields", required = true)
    private List<SimpleDataFieldConfig> fields = new ArrayList<SimpleDataFieldConfig>();

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.None;
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.None;
    }

    public List<SimpleDataFieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<SimpleDataFieldConfig> fields) {
        this.fields = fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleDataConfig that = (SimpleDataConfig) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fields);
    }
}
