package ru.intertrust.cm.core.config.gui.form.title;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.10.2014
 *         Time: 17:51
 */
@Root(name = "title")
public class TitleConfig implements Dto {
    @Element(name = "new-object", required = true)
    private NewObjectConfig newObjectConfig;

    @Element(name = "existing-object", required = true)
    private ExistingObjectConfig existingObjectConfig;

    public NewObjectConfig getNewObjectConfig() {
        return newObjectConfig;
    }

    public void setNewObjectConfig(NewObjectConfig newObjectConfig) {
        this.newObjectConfig = newObjectConfig;
    }

    public ExistingObjectConfig getExistingObjectConfig() {
        return existingObjectConfig;
    }

    public void setExistingObjectConfig(ExistingObjectConfig existingObjectConfig) {
        this.existingObjectConfig = existingObjectConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TitleConfig that = (TitleConfig) o;

        if (existingObjectConfig != null ? !existingObjectConfig.equals(that.existingObjectConfig)
                : that.existingObjectConfig != null) {
            return false;
        }
        if (newObjectConfig != null ? !newObjectConfig.equals(that.newObjectConfig) : that.newObjectConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = newObjectConfig != null ? newObjectConfig.hashCode() : 0;
        result = 31 * result + (existingObjectConfig != null ? existingObjectConfig.hashCode() : 0);
        return result;
    }
}
