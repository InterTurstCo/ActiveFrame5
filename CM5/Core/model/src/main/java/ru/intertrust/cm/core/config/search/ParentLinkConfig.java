package ru.intertrust.cm.core.config.search;

import org.simpleframework.xml.Attribute;

import ru.intertrust.cm.core.config.DoelAware;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParentLinkConfig extends DoelAware {

    private static final String DELIMITER = ",";

    @Attribute(required = false)
    private String types;

    /**
     * Coma delimited String to replace AnyType in your DoEL expression.
     * E.g., if your DOP can be AnyType, you can add the specific type
     * to list to resolve the DoEL path
     */
    public String getTypes() {
        return types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ParentLinkConfig that = (ParentLinkConfig) o;
        return Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), types);
    }

    @Nullable
    public String[] getTypesAsArray() {
        return types == null ? null : types.split(DELIMITER);
    }
}
