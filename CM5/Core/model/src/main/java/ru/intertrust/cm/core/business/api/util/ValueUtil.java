package ru.intertrust.cm.core.business.api.util;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Lesia Puhova
 *         Date: 25.03.14
 *         Time: 10:59
 */
public class ValueUtil {

    private ValueUtil() {} // non-instantiable

    public static String valueToString(Value value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        switch (value.getFieldType()) {
            case STRING:
            case TEXT:
            case PASSWORD:
                return (String)value.get();
            case BOOLEAN:
            case DECIMAL:
            case LONG:
                return value.get().toString();
            case DATETIMEWITHTIMEZONE:
                return format((DateTimeWithTimeZone) (value.get()));
            case DATETIME:
                return ((Date)value.get()).getTime() + ""; // number of milliseconds as a string
            case TIMELESSDATE:
                return format((TimelessDate) (value.get()));
            case REFERENCE:
                return ((Id)(value.get())).toStringRepresentation();
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Value stringValueToObject(String string, String fieldType) {
        return stringValueToObject(string, FieldType.valueOf(fieldType));
    }

    public static Value stringValueToObject(String string, FieldType fieldType) {
        switch (fieldType) {
            case STRING:
            case TEXT:
            case PASSWORD:
                return new StringValue(string);
            case BOOLEAN:
                return new BooleanValue("true".equals(string) ? Boolean.TRUE : Boolean.FALSE);
            case LONG:
                return new LongValue(Long.parseLong(string));
            case DECIMAL:
                return new DecimalValue(new BigDecimal(string));
            case DATETIMEWITHTIMEZONE:
                return parseDateTimeWithTimezone(string);
            case DATETIME:
                return new DateTimeValue(new Date(Long.parseLong(string)));
            case TIMELESSDATE:
                return parseTimelessDate(string);
            case REFERENCE:
                Id id = new RdbmsId();
                id.setFromStringRepresentation(string);
                return new ReferenceValue(id);
            default: return new StringValue(string);
        }
    }

    private static Value parseTimelessDate(String string) {
        //TODO: [report-plugin] parse from string
        return null;
    }

    private static Value parseDateTimeWithTimezone(String string) {
        //TODO: [report-plugin] parse from string
        return null;
    }


    private static String format(TimelessDate timelessDate) {
        StringBuilder sb = new StringBuilder();
        //TODO: [report-plugin] convert to string
        return sb.toString();
    }

    private static String format(DateTimeWithTimeZone value) {
        StringBuilder sb = new StringBuilder();
        //TODO: [report-plugin] convert to string
        return sb.toString();
    }

}
