package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.UniqueKeyInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс-маппер для извлечения данных о уникальных ключах из ResultSet
 * Created by vmatsukevich on 27.1.15.
 */
public class UniqueKeysRowMapper implements ResultSetExtractor<Map<String, Map<String, UniqueKeyInfo>>> {

    /**
     * Ихвлекает данные о уникальных ключах из ResultSet
     * @param rs
     * @return
     * @throws java.sql.SQLException
     * @throws org.springframework.dao.DataAccessException
     */
    @Override
    public Map<String, Map<String, UniqueKeyInfo>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, Map<String, UniqueKeyInfo>> result = new HashMap<>();

        while (rs.next()) {
            String tableName = rs.getString("table_name");
            Map<String, UniqueKeyInfo> foreignKeys = result.get(tableName);

            if (foreignKeys == null) {
                foreignKeys = new HashMap<>();
                result.put(tableName, foreignKeys);
            }

            String constraintName = rs.getString("constraint_name");
            UniqueKeyInfo uniqueKeyInfo = foreignKeys.get(constraintName);

            if (uniqueKeyInfo == null) {
                uniqueKeyInfo = new UniqueKeyInfo();
                foreignKeys.put(constraintName, uniqueKeyInfo);

                uniqueKeyInfo.setName(constraintName);
                uniqueKeyInfo.setTableName(tableName);
            }

            uniqueKeyInfo.getColumnNames().add(rs.getString("column_name"));
        }

        return result;
    }
}
