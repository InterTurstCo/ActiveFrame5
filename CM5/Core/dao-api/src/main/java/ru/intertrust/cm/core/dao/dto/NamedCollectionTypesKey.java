package ru.intertrust.cm.core.dao.dto;

import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 11.08.2015
 *         Time: 16:49
 */
public class NamedCollectionTypesKey implements CollectionTypesKey {
    protected String name;
    protected Set<String> filterNames;

    public NamedCollectionTypesKey(String name, Set<String> filterNames) {
        this.name = name;
        this.filterNames = filterNames;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof NamedCollectionTypesKey)) {
            return false;
        }

        NamedCollectionTypesKey that = (NamedCollectionTypesKey) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (filterNames != null ? !filterNames.equals(that.filterNames) : that.filterNames != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (filterNames != null ? filterNames.hashCode() : 0);
        return result;
    }
}
