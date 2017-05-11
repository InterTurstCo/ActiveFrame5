package ru.intertrust.cm.core.business.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Value;

/**
 * Обертка для {@link Filter} с переопределенным методом equals() для
 * кеширования запросов по фильтрам. В методе equals() учитываются только
 * название фильтра и названия параметров.
 * @author atsvetkov
 */
public class FilterForCache {
    private String filterName;
    private Set<Integer> paramNames;

    public FilterForCache(Filter filter) {
        this.filterName = filter.getFilter();
        HashMap<Integer, List<Value>> parameterMap = filter.getParameterMap();
        this.paramNames = parameterMap == null ? null : new HashSet<>(parameterMap.keySet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FilterForCache that = (FilterForCache) o;
        return Objects.equals(filterName, that.filterName) &&
                Objects.equals(paramNames, that.paramNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterName, paramNames);
    }

    public int getParamsCount() {
        return paramNames == null ? 0 : paramNames.size();
    }
}