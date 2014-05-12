package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.LongValue;

import java.text.DecimalFormat;

/**
 * Created by andrey on 16.01.14.
 */
class NumberFormatter implements FieldFormatter {
    private DataExtractor dataExtractor;
    private DomainObject domainObject;

    public NumberFormatter(DataExtractor dataExtractor) {
        this.dataExtractor = dataExtractor;
    }

    NumberFormatter(DomainObject domainObject) {
        this.domainObject = domainObject;
    }

    @Override
    public String format(String fieldName, String formatPattern) {
        LongValue fieldValue =  domainObject.getValue(fieldName);
        if (fieldValue != null) {
            return formatWithNumberPattern(fieldValue.get(), formatPattern);
        }
        return null;
    }

    private String formatWithNumberPattern(Long value, String formatPattern) {
        DecimalFormat decimalFormat = new DecimalFormat(formatPattern);
        return decimalFormat.format(value);
    }
}
