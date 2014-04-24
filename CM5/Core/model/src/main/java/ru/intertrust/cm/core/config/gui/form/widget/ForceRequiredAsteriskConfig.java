package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.4.14
 *         Time: 13:15
 */
@Root(name = "force-required-asterisk")
public class ForceRequiredAsteriskConfig implements Dto {
    @Attribute(name = "value")
    private boolean forceRequiredAsterisk;

    public boolean isForceRequiredAsterisk() {
        return forceRequiredAsterisk;
    }

    public void setValue(boolean forceRequiredAsterisk) {
        this.forceRequiredAsterisk = forceRequiredAsterisk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ForceRequiredAsteriskConfig that = (ForceRequiredAsteriskConfig) o;

        if (forceRequiredAsterisk != that.forceRequiredAsterisk) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (forceRequiredAsterisk ? 1 : 0);
    }
}
