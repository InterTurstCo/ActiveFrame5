package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 17:13.
 */
public class ReferenceTypeConfig implements Dto {

    @Attribute(name = "field-path")
    private String fieldPath;

    public String getFieldPath() {
        return fieldPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferenceTypeConfig that = (ReferenceTypeConfig) o;

        if (fieldPath != null ? !fieldPath.equals(that.fieldPath) : that.fieldPath != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return fieldPath != null ? fieldPath.hashCode() : 0;
    }
}
