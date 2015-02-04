package ru.intertrust.cm.core.business.api.util;

import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeValue;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.DecimalValue;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.LongValue;
import ru.intertrust.cm.core.business.api.dto.OlsonTimeZoneContext;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.TimeZoneContext;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.UTCOffsetTimeZoneContext;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.model.GwtIncompatible;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 25.03.14
 *         Time: 10:59
 */
public class ValueUtil {

    private static final String ITEMS_DELIMITER = "_"; //TODO: need to escape them in string values
    private static final String ITEM_TYPE_VALUE_DELIMItER = "-";

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
            case LIST:
                StringBuilder sb = new StringBuilder();
                int i = 0;
                List<Serializable> items = ((ListValue)value).get();
                for (Serializable item : items) {
                    if (item instanceof Id) {
                        sb.append(((Id)item).toStringRepresentation());
                    } else {
                        sb.append(item);
                    }
                    sb.append(ITEM_TYPE_VALUE_DELIMItER)
                      .append(ValueUtil.getFieldTypeForListItem(item));

                    if (++i <  items.size()) {
                        sb.append(ITEMS_DELIMITER);
                    }
                }
                return sb.toString();
            default:
                throw new IllegalArgumentException();
        }
    }

    @GwtIncompatible
    public static Value stringValueToObject(String string, String fieldType) {
        return stringValueToObject(string, FieldType.valueOf(fieldType));
    }

    @GwtIncompatible
    public static Value stringValueToObject(String string, FieldType fieldType) {
        switch (fieldType) {
            case STRING:
            case TEXT:
            case PASSWORD:
                return new StringValue(string);
            case BOOLEAN:
                if (string == null) {
                    return new BooleanValue(null);
                }
                return new BooleanValue("true".equals(string) ? Boolean.TRUE : Boolean.FALSE);
            case LONG:
                if (string == null) {
                    return new LongValue((Long) null);
                }
                return new LongValue(Long.parseLong(string));
            case DECIMAL:
                if (string == null) {
                    return new DecimalValue((Long) null);
                }
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
            case LIST:
                List<Value> values = new ArrayList<>();
                String[] chunks = string.split(ITEMS_DELIMITER);
                for (String chunk : chunks) {
                    if (chunk != null && !chunk.isEmpty()) {
                        String[]parts = chunk.split(ITEM_TYPE_VALUE_DELIMItER);
                        String paramValue = parts[0];
                        String paramType = parts[1];
                        if (paramValue != null && !"null".equals(paramValue)) {
                            Value value = ValueUtil.stringValueToObject(paramValue, paramType);
                            values.add(value);
                        }
                    }
                }
                return new ListValue(values);
            default: return new StringValue(string);
        }
    }

    private static FieldType getFieldTypeForListItem(Serializable plainValue) {
        if (plainValue instanceof Boolean) {
            return FieldType.BOOLEAN;
        }
        if (plainValue instanceof String)  {
            return FieldType.STRING;
        }
        if (plainValue instanceof Long) {
            return FieldType.LONG;
        }
        if (plainValue instanceof BigDecimal) {
            return FieldType.DECIMAL;
        }
        if (plainValue instanceof DateTimeWithTimeZone) {
            return FieldType.DATETIMEWITHTIMEZONE;
        }
        if (plainValue instanceof Date) {
            return FieldType.DATETIME;
        }
        if (plainValue instanceof TimelessDate) {
            return FieldType.TIMELESSDATE;
        }
        if (plainValue instanceof Id) {
            return FieldType.REFERENCE;
        }
        throw new IllegalArgumentException("Object has non-supported type: " + plainValue);
    }

    private static String format(TimelessDate timelessDate) {
        StringBuilder sb = new StringBuilder();
        sb.append(timelessDate.getDayOfMonth()).append("/")
                .append(timelessDate.getMonth() + 1).append("/")
                .append(timelessDate.getYear());
        return sb.toString();
    }

    private static String format(DateTimeWithTimeZone dateTime) {
        StringBuilder sb = new StringBuilder();
        int day = dateTime.getDayOfMonth();
        int month = dateTime.getMonth() + 1;
        int year = dateTime.getYear();
        int hours = dateTime.getHours();
        int minutes = dateTime.getMinutes();
        int seconds = dateTime.getSeconds();
        int milliseconds = dateTime.getMilliseconds();

        TimeZoneContext context = dateTime.getTimeZoneContext();
        if (context instanceof UTCOffsetTimeZoneContext) {
            long offset = ((UTCOffsetTimeZoneContext)context).getOffset();
            sb.append("UTC ")
                    .append(day).append("/")
                    .append(month).append("/")
                    .append(year).append(" ")
                    .append(hours).append(":")
                    .append(minutes).append(":")
                    .append(seconds).append(":")
                    .append((milliseconds)).append(" ")
                    .append(offset);
        } else if (context instanceof OlsonTimeZoneContext) {
            String timeZoneId = ((OlsonTimeZoneContext)context).getTimeZoneId();
            sb.append("Olson ")
                    .append(day).append("/")
                    .append(month).append("/")
                    .append(year).append(" ")
                    .append(hours).append(":")
                    .append(minutes).append(":")
                    .append(seconds).append(":")
                    .append((milliseconds)).append(" ")
                    .append(timeZoneId);
        } else {
            throw new IllegalArgumentException("Unsupported TimeZoneContext: " + context);
        }


        return sb.toString();
    }

    @GwtIncompatible
    private static Value parseTimelessDate(String string) {
        String[] date = string.split("/");
        int day = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]) - 1; // month is 0-based in timelessDate
        int year = Integer.parseInt(date[2]);
        TimelessDate timelessDate = new TimelessDate(year, month, day);
        return new TimelessDateValue(timelessDate);
    }

    @GwtIncompatible
    private static Value parseDateTimeWithTimezone(String string) {
        DateTimeWithTimeZone dateTimeZone;
        String[] parts = string.split(" "); // [context type id, date, time, offset or timezone id]

        String[] date = parts[1].split("/");
        int day = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]) - 1; // month is 0-based in DateTimeWithTimeZone
        int year = Integer.parseInt(date[2]);
        String[] time = parts[2].split(":");
        int hours = Integer.parseInt(time[0]);
        int minutes = Integer.parseInt(time[1]);
        int seconds = Integer.parseInt(time[2]);
        int milliseconds = Integer.parseInt(time[3]);
        if ("UTC".equals(parts[0])) {
            int timeZoneUtcOffset = Integer.parseInt(parts[3]);
            dateTimeZone = new DateTimeWithTimeZone(timeZoneUtcOffset, year, month, day, hours, minutes, seconds, milliseconds);
        } else if("Olson".equals(parts[0])) {
            String timeZoneId = parts[3];
            dateTimeZone = new DateTimeWithTimeZone(timeZoneId, year, month, day, hours, minutes, seconds, milliseconds);
        } else {
            throw new IllegalArgumentException("Cannot parse string: " + string + ". Unsupported TimeZoneContext id: " + parts[0]);
        }

        return new DateTimeWithTimeZoneValue(dateTimeZone);
    }

}
