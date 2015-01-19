package ru.intertrust.cm.core.gui.impl.server.widget;

import java.util.Date;

import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.util.ThreadSafeDateFormat;

/**
 * Created by andrey on 16.01.14.
 */
class DateFormatter implements FieldFormatter {
    private DataExtractor dataExtractor;
    private DomainObject domainObject;

    public DateFormatter(DataExtractor dataExtractor) {
        this.dataExtractor = dataExtractor;
    }

    DateFormatter(DomainObject domainObject) {
        this.domainObject = domainObject;
    }

    @Override
    public String format(String fieldName, String formatPattern) {
        DateTimeValue timestamp = domainObject.getValue(fieldName);
        if (timestamp != null) {
            return formatWithDatePattern(timestamp.get(), formatPattern);
        }
        return null;
    }

    private String formatWithDatePattern(Date value, String formatPattern) {
        return ThreadSafeDateFormat.format(value, formatPattern);
    }
}
