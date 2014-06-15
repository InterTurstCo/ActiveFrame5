package ru.intertrust.cm.core.config.gui.form.widget.filter;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.06.14
 *         Time: 13:15
 */
public class AbstractFiltersConfig implements Dto {
   // @ElementList(type=AbstractFilterConfig.class, inline=true)
   @ElementListUnion({
           @ElementList(entry = "initial-filter", type = InitialFilterConfig.class, inline = true, required = false),
           @ElementList(entry = "selection-filter", type = SelectionFilterConfig.class, inline = true, required = false)
   })
    private List<AbstractFilterConfig> abstractFilterConfigs;

    public List<AbstractFilterConfig> getAbstractFilterConfigs() {
        return abstractFilterConfigs;
    }

    public void setAbstractFilterConfigs(List<AbstractFilterConfig> abstractFilterConfigs) {
        this.abstractFilterConfigs = abstractFilterConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractFiltersConfig that = (AbstractFiltersConfig) o;

        if (abstractFilterConfigs != null ? !abstractFilterConfigs.equals(that.abstractFilterConfigs) :
                that.abstractFilterConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return abstractFilterConfigs != null ? abstractFilterConfigs.hashCode() : 0;
    }
}
