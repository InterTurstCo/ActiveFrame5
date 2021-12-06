package ru.intertrust.cm.core.config.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public class CompoundFieldsConfig implements Serializable {

    @ElementList(name = "field-part", inline = true)
    private List<CompoundFieldConfig> fieldPart = new ArrayList<>();

    @Attribute
    private String delimiter;

    public List<CompoundFieldConfig> getFieldPart() {
        return fieldPart;
    }

    public String getDelimiter() {
        return delimiter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompoundFieldsConfig)) return false;
        final CompoundFieldsConfig that = (CompoundFieldsConfig) o;
        return Objects.equals(getFieldPart(), that.getFieldPart()) && Objects.equals(getDelimiter(), that.getDelimiter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFieldPart(), getDelimiter());
    }
}
