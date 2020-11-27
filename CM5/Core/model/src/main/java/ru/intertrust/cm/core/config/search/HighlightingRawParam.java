package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;

import java.io.Serializable;

public class HighlightingRawParam implements Serializable {
    @Attribute(name = "name", required = true)
    private String name;

    @Attribute(name = "value", required = true)
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HighlightingRawParam)) {
            return false;
        }
        HighlightingRawParam other = (HighlightingRawParam) obj;
        return (this.name == null ? other.name == null : this.name.equals(other.name))
                && (this.value == null ? other.value == null : this.value.equals(other.value));
    }

    @Override
    public int hashCode() {
        int hash = name != null ? name.hashCode() : 12345;
        hash *= 31 ^ (value != null ? value.hashCode() : 0);
        return hash;
    }
}
