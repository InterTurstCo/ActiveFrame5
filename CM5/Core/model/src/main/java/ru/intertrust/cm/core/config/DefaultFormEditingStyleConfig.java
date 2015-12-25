package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.ToggleEditConfig;

import java.io.Serializable;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 23.12.2015
 */
@Root(name = "default-form-editing-style")
public class DefaultFormEditingStyleConfig implements Serializable {

    @Element(name = "toggle-edit")
    private ToggleEditConfig toggleEditConfig;

    public ToggleEditConfig getToggleEditConfig() {
        return toggleEditConfig;
    }

    public void setToggleEditConfig(ToggleEditConfig toggleEditConfig) {
        this.toggleEditConfig = toggleEditConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultFormEditingStyleConfig that = (DefaultFormEditingStyleConfig) o;

        if (toggleEditConfig != null ? !toggleEditConfig.equals(that.toggleEditConfig) : that.toggleEditConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = toggleEditConfig != null ? toggleEditConfig.hashCode() : 0;
        result = 31 * result + super.hashCode();
        return result;
    }
}
