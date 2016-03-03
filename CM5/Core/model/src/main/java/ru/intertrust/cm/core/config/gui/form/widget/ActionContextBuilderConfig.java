package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by 1 on 18.02.2016.
 */
@Root(name = "action-context-builder")
public class ActionContextBuilderConfig implements Dto {
    @Attribute(name = "component")
    private String builderComponent;

    public String getBuilderComponent() {
        return builderComponent;
    }

    public void setBuilderComponent(String builderComponent) {
        this.builderComponent = builderComponent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ActionContextBuilderConfig that = (ActionContextBuilderConfig) o;

        if (!builderComponent.equals(that.builderComponent)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (builderComponent!=null ? 1 : 0);
    }
}
