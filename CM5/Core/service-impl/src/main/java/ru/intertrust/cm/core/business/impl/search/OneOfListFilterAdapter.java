package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.OneOfListFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneOfListFilterAdapter implements FilterAdapter<OneOfListFilter> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String getFilterString(OneOfListFilter filter, SearchQuery query) {
        List<ReferenceValue> values = filter.getValues();
        if (values.size() == 0) {
            log.warn("Empty list search filter for " + filter.getFieldName() + " ignored");
            return null;
        }

        StringBuilder str = new StringBuilder()
                .append(SolrFields.FIELD_PREFIX)
                .append(SearchFieldType.REF.getInfix())
                .append(filter.getFieldName().toLowerCase())
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
