package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * User: IPetrov
 * Date: 03.01.14
 * Time: 12:53
 * Область поиска. Используется для расширенного поиска.
 */
@Root(name = "search-area-ref")
public class SearchAreaRefConfig implements Dto {

    @Attribute(name = "name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SearchAreaRefConfig that = (SearchAreaRefConfig) o;

        if (name != null ? !name.equals(that.getName()) : that.getName() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        return result;
    }
}
