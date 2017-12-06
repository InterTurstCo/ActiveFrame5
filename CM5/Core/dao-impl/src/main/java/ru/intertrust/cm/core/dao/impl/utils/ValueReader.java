package ru.intertrust.cm.core.dao.impl.utils;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.model.FatalException;

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

        if (fieldConfig == null) {
            return null;
        }

        if (StringFieldConfig.class.equals(fieldConfig.getClass()) ||
                TextFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readStringValue(rs, column);
        } else if (LongFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readLongValue(rs, column);
        } else if (DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readDecimalValue(rs, column);
        } else if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readReferenceValue(rs, columns, columnIndex);
        } else if (DateTimeFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readDateTimeValue(rs, column);
        } else if (DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readDateTimeWithTimeZoneValue(rs, columns, columnIndex);
        } else if (TimelessDateFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readTimelessDateValue(rs, column);
        } else if (BooleanFieldConfig.class.equals(fieldConfig.getClass())) {
            value = readBooleanValue(rs, column);
        }

        return value;
    }

    protected RdbmsId readId(ResultSet rs, Column column) throws SQLException {
        Number longValue = (Number) rs.getObject(column.getIndex());
        if (longValue == null) {
            return null;
        }

        Number idType = (Number) rs.getObject(column.getIndex() + 1);

        if (idType == null) {
            return null;
        }

        return new RdbmsId(idType.intValue(), longValue.longValue());
    }

    @Deprecated
    protected RdbmsId readId(ResultSet rs, String columnName) throws SQLException {
        Number longValue = (Number) rs.getObject(columnName);
        if (longValue == null) {
            return null;
        }

        Number idType = (Number) rs.getObject(Case.toLower(getReferenceTypeColumnName(columnName)));
        if (idType == null) {
            return null;
        }

        return new RdbmsId(idType.intValue(), longValue.longValue());
    }

    protected LongValue readLongValue (ResultSet rs, BasicRowMapper.Column column) throws SQLException {
        Number longValue = (Number) rs.getObject(column.getIndex());
        if (longValue != null) {
            return new LongValue(longValue.longValue());
        } else {
            return new LongValue();
        }
    }

    protected DecimalValue readDecimalValue(ResultSet rs, BasicRowMapper.Column column) throws SQLException {
        return new DecimalValue(rs.getBigDecimal(column.getIndex()));
    }

    protected StringValue readStringValue(ResultSet rs, BasicRowMapper.Column column) throws SQLException {
        return new StringValue(rs.getString(column.getIndex()));
    }

    protected ReferenceValue readReferenceValue(ResultSet rs, List<BasicRowMapper.Column> columns, int columnIndex)
            throws SQLException {
        BasicRowMapper.Column column = columns.get(columnIndex);

        Number longValue = (Number) rs.getObject(column.getIndex());
        if (longValue == null) {
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

            Number typeId = (Number) rs.getObject(nextColumn.getIndex());
            if (typeId != null) {
                return new ReferenceValue(new RdbmsId(typeId.intValue(), longValue.longValue()));
            } else {
                throw new FatalException("Reference type field can not be null for column " + column);
            }
        }
    }

    @Deprecated
    protected ReferenceValue readReferenceValue(ResultSet rs, String columnName)
            throws SQLException {
        Number longValue = (Number) rs.getObject(columnName);
        if (longValue == null) {
            return new ReferenceValue();
        } else {
            String typeColumnName = getReferenceTypeColumnName(columnName);
            Number typeId = (Number) rs.getObject(typeColumnName);
            if (typeId != null) {
                return new ReferenceValue(new RdbmsId(typeId.intValue(), longValue.longValue()));
            } else {
                throw new FatalException("Reference type field can not be null for column " + columnName);
            }
        }
    }

    protected DateTimeValue readDateTimeValue(ResultSet rs, BasicRowMapper.Column column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column.getIndex(), gmtCalendar.get());

        DateTimeValue value;
        if (timestamp != null) {
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
        if (timestamp != null) {
            calendar.setTime(timestamp);

            value = new TimelessDateValue(new TimelessDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ));
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
        if (timestamp != null) {
            if (columns.size() == columnIndex + 1) {
                throw new FatalException("TimeZone id field can not be null for column " + column.getName());
            }

            BasicRowMapper.Column nextColumn = columns.get(columnIndex + 1);
            String timeZoneIdColumnName = Case.toLower(getTimeZoneIdColumnName(column.getName()));
            if (!nextColumn.getName().equalsIgnoreCase(timeZoneIdColumnName)) {
                throw new FatalException("TimeZone id field can not be null for column " + column.getName());
            }

            String timeZoneId = rs.getString(nextColumn.getIndex());
            if (timeZoneId != null) {
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
        Number booleanInt = (Number) rs.getObject(column.getIndex());
        if (booleanInt != null) {
            return booleanInt.intValue() == 1 ? BooleanValue.TRUE : BooleanValue.FALSE;
        } else {
            return BooleanValue.EMPTY;
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
        Calendar calendar = getThreadLocalCalendar(timeZoneId);
        calendar.setTime(timestamp);

        return new DateTimeWithTimeZone(
            getDateTimeWithTimeZoneContext(timeZoneId),
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
            calendar.get(Calendar.MILLISECOND)
        );
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
