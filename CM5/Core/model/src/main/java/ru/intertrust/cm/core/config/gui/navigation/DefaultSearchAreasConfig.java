package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * User: IPetrov
 * Date: 03.01.14
 * Time: 13:18
 * Область(и) поиска по умолчанию. Используется для расширенного поиска.
 */
@Root(name = "default-search-areas")
public class DefaultSearchAreasConfig implements Dto {

    @Attribute(name = "name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ElementList(inline = true, required = false)
    private List<SearchAreaRefConfig> searchAreaRefConfig = new ArrayList<SearchAreaRefConfig>();

    public List<SearchAreaRefConfig> getSearchAreaRefConfig() {
        return searchAreaRefConfig;
    }

    public void setSearchAreaRefConfig(List<SearchAreaRefConfig> searchAreaRefConfig) {
        this.searchAreaRefConfig = searchAreaRefConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultSearchAreasConfig that = (DefaultSearchAreasConfig) o;

        if (searchAreaRefConfig != null ? !searchAreaRefConfig.equals(that.getSearchAreaRefConfig()) : that.
                getSearchAreaRefConfig() != null) {
            return false;
        }

        if (name != null ? !name.equals(that.getName()) : that.getName() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (searchAreaRefConfig != null ? searchAreaRefConfig.hashCode() : 0);
        return result;
    }
}
