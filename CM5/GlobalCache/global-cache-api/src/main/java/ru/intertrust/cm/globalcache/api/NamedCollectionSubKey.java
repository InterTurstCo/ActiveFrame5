package ru.intertrust.cm.globalcache.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.DomainEntitiesCloner;

/**
 * @author Denis Mitavskiy Date: 13.08.2015 Time: 17:06
 */
public class NamedCollectionSubKey extends CollectionSubKey {
    public final Set<? extends Filter> filterValues;
    public final SortOrder sortOrder;

    public NamedCollectionSubKey(UserSubject subject, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit) {
        super(subject, offset, limit);
        this.filterValues = filterValues == null ? null : new HashSet<>(filterValues);
        this.sortOrder = sortOrder;
    }

    public NamedCollectionSubKey(UserSubject subject, Set<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit) {
        super(subject, offset, limit);
        this.filterValues = filterValues;
        this.sortOrder = sortOrder;
    }

    @Override
    public int getKeyEntriesQty() {
        if (filterValues == null) {
            return 0;
        }
        int qty = 0;
        for (Filter filter : filterValues) {
            if (filter != null) {
                final Collection<List<Value>> values = filter.getParameterMap().values();
                if (values == null || values.isEmpty()) {
                    ++qty;
                    continue;
                }
                for (List<Value> value : values) {
                    if (value != null) {
                        if (value instanceof ListValue) {
                            ListValue listValue = (ListValue) value;
                            List<Value<?>> listValueValues = listValue.getUnmodifiableValuesList();
                            if (listValueValues != null) {
                                qty += listValueValues.size();
                            } else {
                                ++qty;
                            }
                        } else {
                            ++qty;
                        }
                    } else {
                        ++qty;
                    }
                }
            } else {
                ++qty;
            }
        }
        return qty;
    }

    @Override
    public CollectionSubKey getCopy(DomainEntitiesCloner cloner) {
        final HashSet<Filter> filtersClone;
        if (filterValues == null) {
            filtersClone = null;
        } else {
            filtersClone = new HashSet<>(filterValues.size() * 3 / 2);
            for (Filter filter : filterValues) {
                filtersClone.add(cloner.fastCloneFilter(filter));
            }
        }
        return new NamedCollectionSubKey(subject, filtersClone, cloner.fastCloneSortOrder(sortOrder), offset, limit);
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
