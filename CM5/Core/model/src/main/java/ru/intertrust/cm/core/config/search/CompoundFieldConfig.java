package ru.intertrust.cm.core.config.search;

import java.io.Serializable;
import java.util.Objects;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "field-part")
public class CompoundFieldConfig implements Serializable {

    @Element(required = false)
    private String doel;

    @Element(name = "script", required = false)
    private IndexedFieldScriptConfig scriptConfig;

    public String getDoel() {
        return doel;
    }

    public IndexedFieldScriptConfig getScriptConfig() {
        return scriptConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompoundFieldConfig)) return false;
        final CompoundFieldConfig that = (CompoundFieldConfig) o;
        return Objects.equals(getDoel(), that.getDoel()) && Objects.equals(getScriptConfig(), that.getScriptConfig());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDoel(), getScriptConfig());
    }
}
