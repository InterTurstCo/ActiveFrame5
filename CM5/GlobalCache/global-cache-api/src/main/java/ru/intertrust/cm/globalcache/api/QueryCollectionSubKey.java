package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.DomainEntitiesCloner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 13.08.2015
 *         Time: 17:06
 */
public class QueryCollectionSubKey extends CollectionSubKey {
    public final List<? extends Value> paramValues;

    public QueryCollectionSubKey(UserSubject subject, List<? extends Value> paramValues, int offset, int limit) {
        super(subject, offset, limit);
        this.paramValues = paramValues;
    }

    @Override
    public int getKeyEntriesQty() {
        if (paramValues == null) {
            return 0;
        }
        int qty = 0;
        for (Value paramValue : paramValues) {
            if (paramValue != null) {
                if (paramValue instanceof ListValue) {
                    final ArrayList<Value> values = ((ListValue) paramValue).getValues();
                    if (values != null) {
                        qty += values.size();
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
        return new QueryCollectionSubKey(subject, cloner.fastCloneValueList(paramValues), offset, limit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof QueryCollectionSubKey)) {
            return false;
        }

        QueryCollectionSubKey that = (QueryCollectionSubKey) o;

        if (offset != that.offset) {
            return false;
        }
        if (limit != that.limit) {
            return false;
        }
        if (paramValues != null ? !paramValues.equals(that.paramValues) : that.paramValues != null) {
            return false;
        }
        if (subject != null ? !subject.equals(that.subject) : that.subject != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = paramValues != null ? paramValues.hashCode() : 0;
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + offset;
        result = 31 * result + limit;
        return result;
    }
}
