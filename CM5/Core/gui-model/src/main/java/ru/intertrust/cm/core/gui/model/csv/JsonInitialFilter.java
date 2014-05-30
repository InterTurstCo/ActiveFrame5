package ru.intertrust.cm.core.gui.model.csv;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 28.05.14
 *         Time: 16:15
 */
public class JsonInitialFilter {
    private String name;
    private List<JsonFilterParam> filterParams;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JsonFilterParam> getFilterParams() {
        return filterParams;
    }

    public void setFilterParams(List<JsonFilterParam> filterParams) {
        this.filterParams = filterParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JsonInitialFilter that = (JsonInitialFilter) o;

        if (filterParams != null ? !filterParams.equals(that.filterParams) : that.filterParams != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (filterParams != null ? filterParams.hashCode() : 0);
        return result;
    }
}
