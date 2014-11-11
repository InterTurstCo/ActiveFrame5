package ru.intertrust.cm.core.config.gui.form.widget.filter;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.ExtraFilterConfig;
import ru.intertrust.cm.core.config.gui.navigation.InitialFilterConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.06.14
 *         Time: 13:15
 */
public class AbstractFiltersConfig<T extends AbstractFilterConfig> implements Dto {

   @ElementListUnion({
           @ElementList(entry = "initial-filter", type = InitialFilterConfig.class, inline = true, required = false),
           @ElementList(entry = "extra-filter", type = ExtraFilterConfig.class, inline = true, required = false),
           @ElementList(entry = "selection-filter", type = SelectionFilterConfig.class, inline = true, required = false)
   })
    private List<T> filterConfigs;

    public List<T> getFilterConfigs() {
        return filterConfigs;
    }

    public void setFilterConfigs(List<T> abstractFilterConfigs) {
        this.filterConfigs = abstractFilterConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractFiltersConfig that = (AbstractFiltersConfig) o;

        if (filterConfigs != null ? !filterConfigs.equals(that.filterConfigs) :
                that.filterConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return filterConfigs != null ? filterConfigs.hashCode() : 0;
    }
}
