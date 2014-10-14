package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 14.10.2014
 *         Time: 14:55
 */
@Root(name = "form-save-extension")
public class FormSaveExtensionConfig implements Dto {
    @Attribute(name = "before-save-component", required = false)
    private String beforeSaveComponent;

    @Attribute(name = "after-save-component", required = false)
    private String afterSaveComponent;

    public FormSaveExtensionConfig() {
    }

    public String getBeforeSaveComponent() {
        return beforeSaveComponent;
    }

    public void setBeforeSaveComponent(String beforeSaveComponent) {
        this.beforeSaveComponent = beforeSaveComponent;
    }

    public String getAfterSaveComponent() {
        return afterSaveComponent;
    }

    public void setAfterSaveComponent(String afterSaveComponent) {
        this.afterSaveComponent = afterSaveComponent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormSaveExtensionConfig that = (FormSaveExtensionConfig) o;

        if (afterSaveComponent != null ? !afterSaveComponent.equals(that.afterSaveComponent) : that.afterSaveComponent != null) {
            return false;
        }
        if (beforeSaveComponent != null ? !beforeSaveComponent.equals(that.beforeSaveComponent) : that.beforeSaveComponent != null) {
            return false;
        }

        return true;
    }
}
