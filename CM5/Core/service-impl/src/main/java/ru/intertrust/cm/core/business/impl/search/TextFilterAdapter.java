package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.TextSearchFilter;

public class TextFilterAdapter implements FilterAdapter<TextSearchFilter> {

    @Override
    public String getFilterValue(TextSearchFilter filter) {
        StringBuilder value = new StringBuilder()
                .append("(")
                .append(filter.getText())
                .append(")");
        return value.toString();
    }

    @Override
    public String getFieldTypeSuffix(TextSearchFilter filter) {
        return SearchFieldType.TEXT.getSuffix();
    }

}
