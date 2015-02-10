package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Содержит метаданные уникального ключа базы данных
 * Created by vmatsukevich on 27.1.15.
 */
public class UniqueKeyInfo {

    private String name;
    private String tableName;
    private List<String> columnNames = new ArrayList<>();

    /**
     * Возвращает имя уникального ключа
     * @return имя уникального ключа
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает имя уникального ключа
     * @param name имя уникального ключа
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает имя таблицы
     * @return имя таблицы
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Устанавливает имя таблицы
     * @param tableName имя таблицы
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Возвращает список колонок уникального ключа
     * @return список колонок уникального ключа
     */
    public List<String> getColumnNames() {
        return columnNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniqueKeyInfo that = (UniqueKeyInfo) o;

        if (columnNames != null ? !columnNames.equals(that.columnNames) : that.columnNames != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (tableName != null ? !tableName.equals(that.tableName) : that.tableName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        result = 31 * result + (columnNames != null ? columnNames.hashCode() : 0);
        return result;
    }
}
