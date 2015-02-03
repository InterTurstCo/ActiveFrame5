package ru.intertrust.cm.core.business.api.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Содержит метаданные внешнего ключа базы данных
 * Created by vmatsukevich on 27.1.15.
 */
public class ForeignKeyInfo {

    private String name;
    private String tableName;
    private String referencedTableName;
    private Map<String, String> columnNames = new HashMap<>();

    /**
     * Возвращает имя внешнего ключа
     * @return имя внешнего ключа
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает имя внешнего ключа
     * @param name имя внешнего ключа
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
     * Возвращает имя таблицы, на кот. ссылается внешний ключ
     * @return имя таблицы, на кот. ссылается внешний ключ
     */
    public String getReferencedTableName() {
        return referencedTableName;
    }

    /**
     * Устанавливает имя таблицы, на кот. ссылается внешний ключ
     * @param referencedTableName имя таблицы, на кот. ссылается внешний ключ
     */
    public void setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
    }

    /**
     * Возвращает маппинг колонок внешнего ключа
     * @return маппинг колонок внешнего ключа
     */
    public Map<String, String> getColumnNames() {
        return columnNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKeyInfo that = (ForeignKeyInfo) o;

        if (columnNames != null ? !columnNames.equals(that.columnNames) : that.columnNames != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (referencedTableName != null ? !referencedTableName.equals(that.referencedTableName) : that.referencedTableName != null)
            return false;
        if (tableName != null ? !tableName.equals(that.tableName) : that.tableName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
