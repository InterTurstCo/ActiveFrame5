package ru.intertrust.cm.core.dao.dto;

/**
 * @author Denis Mitavskiy
 *         Date: 13.08.2015
 *         Time: 16:03
 */
public class QueryCollectionTypesKey implements CollectionTypesKey {
    private String query;

    public QueryCollectionTypesKey(String query) {
        this.query = query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueryCollectionTypesKey that = (QueryCollectionTypesKey) o;

        if (!query.equals(that.query)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return query.hashCode();
    }
}
