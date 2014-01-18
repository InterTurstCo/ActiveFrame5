package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 18.01.14
 * Time: 16:45
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "sort-criteria")
public class SortCriteriaConfig implements Dto {
    @ElementList(inline = true)
    private List<SortCriterionConfig> sortCriterionConfigs = new ArrayList<SortCriterionConfig>();

    public List<SortCriterionConfig> getSortCriterionConfigs() {
        return sortCriterionConfigs;
    }

    public void setSortCriterionConfigs(List<SortCriterionConfig> sortCriterionConfigs) {
        this.sortCriterionConfigs = sortCriterionConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SortCriteriaConfig that = (SortCriteriaConfig) o;

        if (sortCriterionConfigs != null ? !sortCriterionConfigs.equals(that.sortCriterionConfigs) : that.sortCriterionConfigs != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sortCriterionConfigs != null ? sortCriterionConfigs.hashCode() : 0;
    }
}
