package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.10.13
 *         Time: 19:09
 */
@Root(name = "scanner")
public class ScannerConfig implements Dto {
    @Attribute(name = "enabled", required = true)
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScannerConfig that = (ScannerConfig) o;

        if (enabled != that.enabled) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (enabled ? 1 : 0);
    }
}
