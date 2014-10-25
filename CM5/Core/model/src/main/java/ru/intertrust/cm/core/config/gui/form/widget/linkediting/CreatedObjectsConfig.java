package ru.intertrust.cm.core.config.gui.form.widget.linkediting;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.2014
 *         Time: 6:37
 */
@Root(name = "created-objects")
public class CreatedObjectsConfig implements Dto {
    @ElementList(name = "created-object", type = CreatedObjectConfig.class, inline = true)
    private List<CreatedObjectConfig> createObjectConfigs;

    public List<CreatedObjectConfig> getCreateObjectConfigs() {
        return createObjectConfigs;
    }

    public void setCreateObjectConfigs(List<CreatedObjectConfig> createObjectConfigs) {
        this.createObjectConfigs = createObjectConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CreatedObjectsConfig that = (CreatedObjectsConfig) o;

        if (createObjectConfigs != null ? !createObjectConfigs.equals(that.createObjectConfigs) : that.createObjectConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return createObjectConfigs != null ? createObjectConfigs.hashCode() : 0;
    }
}
