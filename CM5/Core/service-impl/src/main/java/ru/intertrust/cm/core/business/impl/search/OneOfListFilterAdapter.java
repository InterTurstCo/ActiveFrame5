package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.OneOfListFilter;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;

import java.util.List;

public class OneOfListFilterAdapter implements FilterAdapter<OneOfListFilter> {

    @Override
    public String getFilterValue(OneOfListFilter filter) {
        List<ReferenceValue> values = filter.getValues();
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

    @Override
    public String getFieldTypeSuffix(OneOfListFilter filter) {
        return SearchFieldType.TEXT.getSuffix();
    }

}
