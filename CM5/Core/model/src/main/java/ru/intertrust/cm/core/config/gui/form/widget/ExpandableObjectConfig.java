package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 18.09.2015
 */
@Root(name = "expandable-object")
public class ExpandableObjectConfig implements Dto {

    @Attribute(name = "object-name", required = true)
    private String objectName;

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpandableObjectConfig that = (ExpandableObjectConfig) o;

        if (objectName != null ? !objectName.equals(that.objectName) : that.objectName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return objectName != null ? objectName.hashCode() : 0;
    }
}
