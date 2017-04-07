package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 22.12.2015
 */
@Root(name = "toggle-edit")
public class ToggleEditConfig implements Dto {
    @Attribute(name = "switch-to-read-mode-on-save", required = false)
    private Boolean switchToReadModeOnSave;

    public Boolean getSwitchToReadModeOnSave() {
        return switchToReadModeOnSave;
    }

    public void setSwitchToReadModeOnSave(Boolean switchToReadModeOnSave) {
        this.switchToReadModeOnSave = switchToReadModeOnSave;
    }

    @Text
    private Boolean value;

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToggleEditConfig that = (ToggleEditConfig) o;

        if (switchToReadModeOnSave != null ? !switchToReadModeOnSave.equals(that.switchToReadModeOnSave) : that.switchToReadModeOnSave != null)
            return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = switchToReadModeOnSave != null ? switchToReadModeOnSave.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
