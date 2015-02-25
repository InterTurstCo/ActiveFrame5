package ru.intertrust.cm.core.business.api.dto;

/**
 * Представляет метаданные колонки в таблице
 * Created by vmatsukevich on 27.1.15.
 */
public class ColumnInfo {

    private String name;
    private boolean notNull;
    private Integer length;
    private Integer precision;
    private Integer scale;
    private String dataType;

    /**
     * Возвращает имя колонки
     * @return имя колонки
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает имя колонки
     * @param name имя колонки
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает not-null ограничение колонки
     * @return not-null ограничение колонки
     */
    public boolean isNotNull() {
        return notNull;
    }

    /**
     * Устанавливает not-null ограничение колонки
     * @param notNull not-null ограничение колонки
     */
    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    /**
     * Возвращает длину текстовой колонки
     * @return длина текстовой колонки
     */
    public Integer getLength() {
        return length;
    }

    /**
     * Устанавливает длину текстовой колонки
     * @param length длина текстовой колонки
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * Возвращает кол-во знаков десятичного числа в колонке соотв. типа
     * @return кол-во знаков десятичного числа
     */
    public Integer getPrecision() {
        return precision;
    }

    /**
     * Устанавливает кол-во знаков десятичного числа
     * @param precision кол-во знаков десятичного числа
     */
    public void setPrecision(Integer precision) {
        this.precision = precision;
    }


    /**
     * Возвращает кол-во знаков после запятой десятичного числа в колонке воотв. типа
     * @return кол-во знаков после запятой десятичного числа
     */
    public Integer getScale() {
        return scale;
    }

    /**
     * Устанавливает кол-во знаков после запятой десятичного числа
     * @param scale кол-во знаков после запятой десятичного числа
     */
    public void setScale(Integer scale) {
        this.scale = scale;
    }

    /**
     * Возвращает тип данных колонки
     * @return тип данных колонки
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Устанавливает тип данных колонки
     * @param dataType тип данных колонки
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnInfo that = (ColumnInfo) o;

        if (length != that.length) return false;
        if (notNull != that.notNull) return false;
        if (precision != that.precision) return false;
        if (scale != that.scale) return false;
        if (dataType != null ? !dataType.equals(that.dataType) : that.dataType != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (notNull ? 1 : 0);
        result = 31 * result + length;
        result = 31 * result + precision;
        result = 31 * result + scale;
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        return result;
    }
}
