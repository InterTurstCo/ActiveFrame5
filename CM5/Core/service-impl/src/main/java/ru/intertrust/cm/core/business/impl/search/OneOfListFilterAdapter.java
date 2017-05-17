package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.OneOfListFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class OneOfListFilterAdapter implements FilterAdapter<OneOfListFilter> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Autowired private SearchConfigHelper configHelper;

    @Override
    public String getFilterString(OneOfListFilter filter, SearchQuery query) {
        List<ReferenceValue> values = filter.getValues();
        if (values.size() == 0) {
            log.warn("Empty list search filter for " + filter.getFieldName() + " ignored");
            return null;
        }

        Set<SearchFieldType> types = configHelper.getFieldTypes(filter.getFieldName(), query.getAreas());
        if (types.contains(null)) {
            types.add(SearchFieldType.REF);
        }
        if (types.contains(SearchFieldType.REF)) {
            String single = makeSolrFieldFilter(filter.getFieldName(), SearchFieldType.REF, values);
            if (!types.contains(SearchFieldType.REF_MULTI)) {
                return single;
            }
            String multi = makeSolrFieldFilter(filter.getFieldName(), SearchFieldType.REF_MULTI, values);
            return new StringBuilder()
                    .append("(").append(single).append(" OR ").append(multi).append(")")
                    .toString();
        } else if (types.contains(SearchFieldType.REF_MULTI)) {
            return makeSolrFieldFilter(filter.getFieldName(), SearchFieldType.REF_MULTI, values);
        }
        log.warn("Configured fields for field " + filter.getFieldName() + " not found in areas " + query.getAreas());
        return null;
    }

    @Override
    public boolean isCompositeFilter(OneOfListFilter filter) {
        return false;
    }

    private static String makeSolrFieldFilter(String name, SearchFieldType type, List<ReferenceValue> values) {
        StringBuilder str = new StringBuilder()
                .append(SolrFields.FIELD_PREFIX)
                .append(type.infix)
                .append(name.toLowerCase())
                .append(":");
        boolean firstValue = true;
        for (ReferenceValue value : values) {
            str.append(firstValue ? "(" : " OR ")
               .append(value.get().toStringRepresentation());
            firstValue = false;
        }
        return str.append(")").toString();
    }
}
