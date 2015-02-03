package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.ForeignKeyInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс-маппер для извлечения данных о внешних ключах из ResultSet
 * Created by vmatsukevich on 27.1.15.
 */
public class ForeignKeysRowMapper implements ResultSetExtractor<Map<String, Map<String, ForeignKeyInfo>>> {

    /**
     * Ихвлекает данные о внешних ключах из ResultSet
     * @param rs
     * @return
     * @throws SQLException
     * @throws DataAccessException
     */
    @Override
    public Map<String, Map<String, ForeignKeyInfo>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, Map<String, ForeignKeyInfo>> result = new HashMap<>();

        while (rs.next()) {
            String tableName = rs.getString("table_name");
            Map<String, ForeignKeyInfo> foreignKeys = result.get(tableName);

            if (foreignKeys == null) {
                foreignKeys = new HashMap<>();
                result.put(tableName, foreignKeys);
            }

            String constraintName = rs.getString("constraint_name");
            ForeignKeyInfo foreignKeyInfo = foreignKeys.get(constraintName);

            if (foreignKeyInfo == null) {
                foreignKeyInfo = new ForeignKeyInfo();
                foreignKeys.put(constraintName, foreignKeyInfo);

                foreignKeyInfo.setName(constraintName);
                foreignKeyInfo.setTableName(tableName);
                foreignKeyInfo.setReferencedTableName(rs.getString("foreign_table_name"));
            }

            foreignKeyInfo.getColumnNames().put(rs.getString("column_name"), rs.getString("foreign_column_name"));
        }

        return result;
    }
}
