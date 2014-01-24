package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.01.14
 *         Time: 11:40
 */
public class RendererConfig implements Dto {
    @Attribute(name = "component-name", required = false)
    private String componentName;

    public String getValue() {
        return componentName;
    }

    public void setValue(String value) {
        this.componentName = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RendererConfig that = (RendererConfig) o;

        if (componentName != null ? !componentName.equals(that.componentName) : that.componentName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return componentName != null ? componentName.hashCode() : 0;
    }
}

