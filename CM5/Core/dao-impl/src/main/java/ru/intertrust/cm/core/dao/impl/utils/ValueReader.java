package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.model.FatalException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getTimeZoneIdColumnName;

/**
 * Базовй класс для отображения {@link java.sql.ResultSet} на доменные объекты и
 * коллекции.
 *
 * @author atsvetkov
 */
public class ValueReader {

    public ValueReader() {
    }

    protected Value readValue(ResultSet rs, String columnName, FieldConfig fieldConfig) throws SQLException {
        Value value = null;

        if (fieldConfig != null &&
                (StringFieldConfig.class.equals(fieldConfig.getClass()) ||
                        TextFieldConfig.class.equals(fieldConfig.getClass()))) {
            value = readStringValue(rs, columnName);
        } else if (fieldConfig != null && LongFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readLongValue(rs, columnName);
        } else if (fieldConfig != null && DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readDecimalValue(rs, columnName);
        } else if (fieldConfig != null && ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readReferenceValue(rs, columnName, (ReferenceFieldConfig) fieldConfig);
        } else if (fieldConfig != null && DateTimeFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readTimestampValue(rs, columnName);
        } else if (fieldConfig != null && DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readDateTimeWithTimeZoneValue(rs, columnName, (DateTimeWithTimeZoneFieldConfig) fieldConfig);
        } else if (fieldConfig != null && TimelessDateFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readTimelessDateValue(rs, columnName);
        } else if (fieldConfig != null && BooleanFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readBooleanValue(rs, columnName);
        }

        return value;
    }

    protected LongValue readLongValue (ResultSet rs, String columnName) throws SQLException {
        Long longValue = rs.getLong(columnName);
        if (!rs.wasNull()) {
            return new LongValue(longValue);
        } else {
            return new LongValue();
        }
    }

    protected DecimalValue readDecimalValue(ResultSet rs, String columnName) throws SQLException {
        BigDecimal fieldValue = rs.getBigDecimal(columnName);
        if (!rs.wasNull()) {
            return new DecimalValue(fieldValue);
        } else {
            return new DecimalValue();
        }
    }

    protected StringValue readStringValue(ResultSet rs, String columnName) throws SQLException {
        String fieldValue = rs.getString(columnName);
        if (!rs.wasNull()) {
            return new StringValue(fieldValue);
        } else {
            return new StringValue();
        }
    }

    protected ReferenceValue readReferenceValue(ResultSet rs, String columnName, ReferenceFieldConfig fieldConfig)
            throws SQLException {
        Long longValue = rs.getLong(columnName);
        if (rs.wasNull()) {
            return new ReferenceValue();
        } else {
            String typeColumnName = getReferenceTypeColumnName(columnName);
            Integer typeId = rs.getInt(typeColumnName);
            if (!rs.wasNull()) {
                return new ReferenceValue(new RdbmsId(typeId, longValue));
            } else {
                throw new FatalException("Reference type field can not be null for column " + columnName);
            }
        }
    }

    protected TimestampValue readTimestampValue(ResultSet rs, String columnName) throws SQLException {
        Calendar gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Timestamp timestamp = rs.getTimestamp(columnName, gmtCalendar);

        TimestampValue value;
        if (!rs.wasNull()) {
            Date date = new Date(timestamp.getTime());
            value = new TimestampValue(date);
        } else {
            value = new TimestampValue();
        }

        return value;
    }

    protected TimelessDateValue readTimelessDateValue(ResultSet rs, String columnName)
            throws SQLException {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Timestamp timestamp = rs.getTimestamp(columnName, calendar);

        TimelessDateValue value;
        if (!rs.wasNull()) {
            calendar.setTime(timestamp);

            TimelessDate timelessDate = new TimelessDate();
            timelessDate.setYear(calendar.get(Calendar.YEAR));
            timelessDate.setMonth(calendar.get(Calendar.MONTH));
            timelessDate.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));

            value = new TimelessDateValue(timelessDate);
        } else {
            value = new TimelessDateValue();
        }

        return value;
    }

    protected DateTimeWithTimeZoneValue readDateTimeWithTimeZoneValue(ResultSet rs, String columnName,
                                                                      DateTimeWithTimeZoneFieldConfig fieldConfig)
            throws SQLException {
        Calendar gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Timestamp timestamp = rs.getTimestamp(columnName, gmtCalendar);

        DateTimeWithTimeZoneValue value;
        if (!rs.wasNull()) {
            String timeZoneIdColumnName = getTimeZoneIdColumnName(fieldConfig.getName()).toLowerCase();
            String timeZoneId = rs.getString(timeZoneIdColumnName);
            if (!rs.wasNull()) {
                DateTimeWithTimeZone dateTimeWithTimeZone = getDateTimeWithTimeZone(timestamp, timeZoneId);
                value = new DateTimeWithTimeZoneValue(dateTimeWithTimeZone);
            } else {
                throw new FatalException("TimeZone id field can not be null for column " + columnName);
            }
        } else {
            value = new DateTimeWithTimeZoneValue();
        }

        return value;
    }

    protected BooleanValue readBooleanValue(ResultSet rs, String columnName) throws SQLException {
        Integer booleanInt = rs.getInt(columnName);
        if (!rs.wasNull()) {
            return new BooleanValue(booleanInt == 1);
        } else {
            return new BooleanValue();
        }
    }

    private TimeZoneContext getDateTimeWithTimeZoneContext(String timeZoneId) {
        if (timeZoneId.startsWith("GMT")) {
            long offset = Long.parseLong(timeZoneId.substring(4))*3600000;
            return new UTCOffsetTimeZoneContext(offset);
        } else {
            return new OlsonTimeZoneContext(timeZoneId);
        }
    }

    private DateTimeWithTimeZone getDateTimeWithTimeZone(Timestamp timestamp, String timeZoneId) {
        DateTimeWithTimeZone dateTimeWithTimeZone = new DateTimeWithTimeZone();
        dateTimeWithTimeZone.setTimeZoneContext(getDateTimeWithTimeZoneContext(timeZoneId));

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
        calendar.setTime(timestamp);

        dateTimeWithTimeZone.setYear(calendar.get(Calendar.YEAR));
        dateTimeWithTimeZone.setMonth(calendar.get(Calendar.MONTH));
        dateTimeWithTimeZone.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
        dateTimeWithTimeZone.setHours(calendar.get(Calendar.HOUR_OF_DAY));
        dateTimeWithTimeZone.setMinutes(calendar.get(Calendar.MINUTE));
        dateTimeWithTimeZone.setSeconds(calendar.get(Calendar.SECOND));
        dateTimeWithTimeZone.setMilliseconds(calendar.get(Calendar.MILLISECOND));

        return dateTimeWithTimeZone;
    }
}
