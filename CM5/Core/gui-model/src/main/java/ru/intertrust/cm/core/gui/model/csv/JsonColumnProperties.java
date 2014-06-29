package ru.intertrust.cm.core.gui.model.csv;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.04.14
 *         Time: 16:15
 */
public class JsonColumnProperties {
    private String columnName;
    private String fieldName;
    private String fieldType;
    private String filterName;
    private List<String> filterValues;
    private String datePattern;
    private String timePattern;
    private String timeZoneId;
    private String initialFilterValue;

    public JsonColumnProperties() {
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public List<String> getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(List<String> filterValues) {
        this.filterValues = filterValues;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public String getTimePattern() {
        return timePattern;
    }

    public void setTimePattern(String timePattern) {
        this.timePattern = timePattern;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getInitialFilterValue() {
        return initialFilterValue;
    }

    public void setInitialFilterValue(String initialFilterValue) {
        this.initialFilterValue = initialFilterValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JsonColumnProperties that = (JsonColumnProperties) o;

        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) {
            return false;
        }
        if (fieldType != null ? !fieldType.equals(that.fieldType) : that.fieldType != null) {
            return false;
        }
        if (filterName != null ? !filterName.equals(that.filterName) : that.filterName != null) {
            return false;
        }
        if (filterValues != null ? !filterValues.equals(that.filterValues) : that.filterValues != null) {
            return false;
        }
        if (datePattern != null ? !datePattern.equals(that.datePattern) : that.datePattern != null) {
            return false;
        }
        if (timePattern != null ? !timePattern.equals(that.timePattern) : that.timePattern != null) {
            return false;
        }
        if (timeZoneId != null ? !timeZoneId.equals(that.timeZoneId) : that.timeZoneId != null) {
            return false;
        }
        if (columnName != null ? !columnName.equals(that.columnName) : that.columnName != null) {
            return false;
        }
        if (initialFilterValue != null ? !initialFilterValue.equals(that.initialFilterValue) : that.initialFilterValue!= null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fieldName != null ? fieldName.hashCode() : 0;
        result = 31 * result + (fieldType != null ? fieldType.hashCode() : 0);
        result = 31 * result + (filterName != null ? filterName.hashCode() : 0);
        result = 31 * result + (filterValues != null ? filterValues.hashCode() : 0);
        result = 31 * result + (datePattern != null ? datePattern.hashCode() : 0);
        result = 31 * result + (timePattern != null ? timePattern.hashCode() : 0);
        result = 31 * result + (timeZoneId != null ? timeZoneId.hashCode() : 0);
        result = 31 * result + (columnName != null ? columnName.hashCode() : 0);
        result = 31 * result + (initialFilterValue != null ? initialFilterValue.hashCode() : 0);
        return result;
    }
}
