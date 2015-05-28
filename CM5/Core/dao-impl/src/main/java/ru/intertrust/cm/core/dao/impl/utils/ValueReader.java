package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.model.FatalException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getTimeZoneIdColumnName;

/**
 * Базовй класс для отображения {@link java.sql.ResultSet} на доменные объекты и
 * коллекции.
 *
 * @author atsvetkov
 */
public class ValueReader {
    private static final ThreadLocal<Calendar> gmtCalendar = new ThreadLocal<Calendar>() {
        protected Calendar initialValue() {
            return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        }
    };

    private static final ThreadLocal<HashMap<String, Calendar>> nonGmtCalendars = new ThreadLocal<HashMap<String, Calendar>>() {
        @Override
        protected HashMap<String, Calendar> initialValue() {
            return new HashMap<>();
        }
    };

    public ValueReader() {
    }

    protected Value readValue(ResultSet rs, List<BasicRowMapper.Column> columns, int columnIndex,
                              FieldConfig fieldConfig) throws SQLException {
        Value value = null;

        BasicRowMapper.Column column = columns.get(columnIndex);

        if (fieldConfig != null &&
                (StringFieldConfig.class.equals(fieldConfig.getClass()) ||
                        TextFieldConfig.class.equals(fieldConfig.getClass()))) {
            value = readStringValue(rs, column);
        } else if (fieldConfig != null && LongFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readLongValue(rs, column);
        } else if (fieldConfig != null && DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readDecimalValue(rs, column);
        } else if (fieldConfig != null && ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readReferenceValue(rs, columns, columnIndex);
        } else if (fieldConfig != null && DateTimeFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readDateTimeValue(rs, column);
        } else if (fieldConfig != null && DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readDateTimeWithTimeZoneValue(rs, columns, columnIndex);
        } else if (fieldConfig != null && TimelessDateFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readTimelessDateValue(rs, column);
        } else if (fieldConfig != null && BooleanFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readBooleanValue(rs, column);
        }

        return value;
    }

    protected RdbmsId readId(ResultSet rs, Column column) throws SQLException {
        Long longValue = rs.getLong(column.getIndex());
        if (rs.wasNull()) {
            return null;
        }

        int idType = rs.getInt(column.getIndex() + 1);

        if (rs.wasNull()) {
            return null;
        }

        return new RdbmsId(idType, longValue);
    }

    @Deprecated
    protected RdbmsId readId(ResultSet rs, String columnName) throws SQLException {
        Long longValue = rs.getLong(columnName);
        if (rs.wasNull()) {
            return null;
        }

        Integer idType = rs.getInt(getReferenceTypeColumnName(columnName).toLowerCase());
        if (rs.wasNull()) {
            return null;
        }

        return new RdbmsId(idType, longValue);
    }

    protected LongValue readLongValue (ResultSet rs, BasicRowMapper.Column column) throws SQLException {
        Long longValue = rs.getLong(column.getIndex());
        if (!rs.wasNull()) {
            return new LongValue(longValue);
        } else {
            return new LongValue();
        }
    }

    protected DecimalValue readDecimalValue(ResultSet rs, BasicRowMapper.Column column) throws SQLException {
        BigDecimal fieldValue = rs.getBigDecimal(column.getIndex());
        if (!rs.wasNull()) {
            return new DecimalValue(fieldValue);
        } else {
            return new DecimalValue();
        }
    }

    protected StringValue readStringValue(ResultSet rs, BasicRowMapper.Column column) throws SQLException {
        String fieldValue = rs.getString(column.getIndex());
        if (!rs.wasNull()) {
            return new StringValue(fieldValue);
        } else {
            return new StringValue();
        }
    }

    protected ReferenceValue readReferenceValue(ResultSet rs, List<BasicRowMapper.Column> columns, int columnIndex)
            throws SQLException {
        BasicRowMapper.Column column = columns.get(columnIndex);

        Long longValue = rs.getLong(column.getIndex());
        if (rs.wasNull()) {
            return new ReferenceValue();
        } else {
            if (columns.size() == columnIndex + 1) {
                throw new FatalException("Reference type field can not be null for column " + column);
            }

            String typeColumnName = getReferenceTypeColumnName(column.getName());
            BasicRowMapper.Column nextColumn = columns.get(columnIndex + 1);
            if (!nextColumn.getName().equalsIgnoreCase(typeColumnName)) {
                throw new FatalException("Reference type field can not be null for column " + column);
            }

            Integer typeId = rs.getInt(nextColumn.getIndex());
            if (!rs.wasNull()) {
                return new ReferenceValue(new RdbmsId(typeId, longValue));
            } else {
                throw new FatalException("Reference type field can not be null for column " + column);
            }
        }
    }

    @Deprecated
    protected ReferenceValue readReferenceValue(ResultSet rs, String columnName)
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

    protected DateTimeValue readDateTimeValue(ResultSet rs, BasicRowMapper.Column column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column.getIndex(), gmtCalendar.get());

        DateTimeValue value;
        if (!rs.wasNull()) {
            Date date = new Date(timestamp.getTime());
            value = new DateTimeValue(date);
        } else {
            value = new DateTimeValue();
        }

        return value;
    }

    protected TimelessDateValue readTimelessDateValue(ResultSet rs, BasicRowMapper.Column column)
            throws SQLException {
        Calendar calendar = gmtCalendar.get();
        Timestamp timestamp = rs.getTimestamp(column.getIndex(), calendar);

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

    protected DateTimeWithTimeZoneValue readDateTimeWithTimeZoneValue(ResultSet rs, List<BasicRowMapper.Column> columns,
                                                                      int columnIndex)
            throws SQLException {
        BasicRowMapper.Column column = columns.get(columnIndex);

        Calendar gmtCalendar = ValueReader.gmtCalendar.get();
        Timestamp timestamp = rs.getTimestamp(column.getIndex(), gmtCalendar);

        DateTimeWithTimeZoneValue value;
        if (!rs.wasNull()) {
            if (columns.size() == columnIndex + 1) {
                throw new FatalException("TimeZone id field can not be null for column " + column.getName());
            }

            BasicRowMapper.Column nextColumn = columns.get(columnIndex + 1);
            String timeZoneIdColumnName = getTimeZoneIdColumnName(column.getName()).toLowerCase();
            if (!nextColumn.getName().equalsIgnoreCase(timeZoneIdColumnName)) {
                throw new FatalException("TimeZone id field can not be null for column " + column.getName());
            }

            String timeZoneId = rs.getString(nextColumn.getIndex());
            if (!rs.wasNull()) {
                DateTimeWithTimeZone dateTimeWithTimeZone = getDateTimeWithTimeZone(timestamp, timeZoneId);
                value = new DateTimeWithTimeZoneValue(dateTimeWithTimeZone);
            } else {
                throw new FatalException("TimeZone id field can not be null for column " + column.getName());
            }
        } else {
            value = new DateTimeWithTimeZoneValue();
        }

        return value;
    }

    protected BooleanValue readBooleanValue(ResultSet rs, BasicRowMapper.Column column) throws SQLException {
        Integer booleanInt = rs.getInt(column.getIndex());
        if (!rs.wasNull()) {
            return new BooleanValue(booleanInt == 1);
        } else {
            return new BooleanValue();
        }
    }

    private TimeZoneContext getDateTimeWithTimeZoneContext(String timeZoneId) {
        if (timeZoneId.startsWith("GMT")) {
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
            return new UTCOffsetTimeZoneContext(timeZone.getRawOffset());
        } else {
            return new OlsonTimeZoneContext(timeZoneId);
        }
    }

    private DateTimeWithTimeZone getDateTimeWithTimeZone(Timestamp timestamp, String timeZoneId) {
        DateTimeWithTimeZone dateTimeWithTimeZone = new DateTimeWithTimeZone();
        dateTimeWithTimeZone.setTimeZoneContext(getDateTimeWithTimeZoneContext(timeZoneId));

        Calendar calendar = getThreadLocalCalendar(timeZoneId);
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

    private Calendar getThreadLocalCalendar(String timeZoneId) {
        switch (timeZoneId) {
            case "GMT":
                return gmtCalendar.get();
            default:
                final HashMap<String, Calendar> cache = nonGmtCalendars.get();
                Calendar calendar = cache.get(timeZoneId);
                if (calendar == null) {
                    calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId));
                    cache.put(timeZoneId, calendar);
                }
                return calendar;
        }
    }

    /**
     * Модель для хранкения названия колонолк и названия колонки-первичного
     * ключа для доменного объекта.
     *
     * @author atsvetkov
     */
    protected static class ColumnModel {

        private String idField;
        private List<BasicRowMapper.Column> columns;

        public ColumnModel() {
        }

        public List<BasicRowMapper.Column> getColumns() {
            if (columns == null) {
                columns = new ArrayList<>();
            }
            return columns;
        }

        public String getIdField() {
            return idField;
        }

        public void setIdField(String idField) {
            this.idField = idField;
        }
    }

    protected static class Column {

        int index;
        private String name;

        public Column(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
