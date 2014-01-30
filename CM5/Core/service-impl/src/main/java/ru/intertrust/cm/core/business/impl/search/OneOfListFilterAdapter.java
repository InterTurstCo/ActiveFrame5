package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.OneOfListFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SearchFilter;

import java.util.List;

public class OneOfListFilterAdapter implements FilterAdapter {

    @Override
    public String getFilterValue(SearchFilter filter) {
        OneOfListFilter listFilter = (OneOfListFilter) filter;
        List<ReferenceValue> values = listFilter.getValues();
        if (values.size() == 0) {
            return "";
        }

        StringBuilder str = new StringBuilder();
        for (ReferenceValue value : values) {
            str.append(str.length() == 0 ? "(" : " OR ")
               .append(value.get().toStringRepresentation());
        }
        return str.append(")").toString();
    }

}
