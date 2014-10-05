package ru.intertrust.cm.core.config.gui.form.widget.linkediting;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.SortCriterionConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 28.09.2014
 *         Time: 23:04
 */
@Root(name = "selection-sort-criteria")
public class SelectionSortCriteriaConfig implements Dto {
    @ElementList(name = "sort-criterion", inline = true)
    private List<SortCriterionConfig> sortCriterionConfigs;

    public List<SortCriterionConfig> getSortCriterionConfigs() {
        return sortCriterionConfigs;
    }

    public void setSortCriterionConfigs(List<SortCriterionConfig> sortCriterionConfigs) {
        this.sortCriterionConfigs = sortCriterionConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SelectionSortCriteriaConfig that = (SelectionSortCriteriaConfig) o;

        if (sortCriterionConfigs != null ? !sortCriterionConfigs.equals(that.sortCriterionConfigs)
                : that.sortCriterionConfigs != null)  {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return sortCriterionConfigs != null ? sortCriterionConfigs.hashCode() : 0;
    }
}

