package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by tbilyi on 18.06.2014.
 */
@Root(name = "on-unlink")
public class OnUnlinkConfig implements Dto {

    @Element(name = "update", required = false)
    protected UpdateSection updateSection;

    public UpdateSection getUpdateSection() {
        return updateSection;
    }

    public void setUpdateSection(UpdateSection updateSection) {
        this.updateSection = updateSection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnUnlinkConfig that = (OnUnlinkConfig) o;

        if (!updateSection.equals(that.updateSection)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return updateSection.hashCode();
    }
}
