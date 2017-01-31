package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.dao.access.UserSubject;

import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 13.08.2015
 *         Time: 17:06
 */
public class NamedCollectionSubKey extends CollectionSubKey {
    public final Set<? extends Filter> filterValues;
    public final SortOrder sortOrder;

    public NamedCollectionSubKey(UserSubject subject, Set<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit) {
        super(subject, offset, limit);
        this.filterValues = filterValues;
        this.sortOrder = sortOrder;
    }

    @Override
    public int getKeyEntriesQty() {
        return filterValues == null ? 0 : filterValues.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof NamedCollectionSubKey)) {
            return false;
        }

        NamedCollectionSubKey that = (NamedCollectionSubKey) o;

        if (offset != that.offset) {
            return false;
        }
        if (limit != that.limit) {
            return false;
        }
        if (filterValues != null ? !filterValues.equals(that.filterValues) : that.filterValues != null) {
            return false;
        }
        if (sortOrder != null ? !sortOrder.equals(that.sortOrder) : that.sortOrder != null) {
            return false;
        }
        if (subject != null ? !subject.equals(that.subject) : that.subject != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = filterValues != null ? filterValues.hashCode() : 0;
        result = 31 * result + (sortOrder != null ? sortOrder.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + offset;
        result = 31 * result + limit;
        return result;
    }
}
