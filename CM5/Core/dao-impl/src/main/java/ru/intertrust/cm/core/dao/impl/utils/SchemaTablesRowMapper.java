package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.ColumnInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс-маппер для извлечения метаданных таблиц из ResultSet
 * Created by vmatsukevich on 27.1.15.
 */
public class SchemaTablesRowMapper implements ResultSetExtractor<Map<String, Map<String, ColumnInfo>>> {

    /**
     * Извлекает метаданные таблиц из ResultSet
     * @param rs ResultSet
     * @return метаданные таблиц
     * @throws SQLException
     * @throws DataAccessException
     */
    @Override
    public Map<String, Map<String, ColumnInfo>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, Map<String, ColumnInfo>> result = new HashMap<>();

        while (rs.next()) {
            String tableName = rs.getString("table_name");
            Map<String, ColumnInfo> columns = result.get(tableName);

            if (columns == null) {
                columns = new HashMap<>();
                result.put(tableName, columns);
            }

            ColumnInfo columnInfo = new ColumnInfo();
            String columnName = rs.getString("column_name");
            columns.put(columnName, columnInfo);

            columnInfo.setName(columnName);
            columnInfo.setDataType(rs.getString("data_type"));

            String nullable = rs.getString("nullable");
            columnInfo.setNotNull(nullable != null && (nullable.equals("N") || nullable.equals("NO")));

            columnInfo.setLength(rs.getString("length") != null ? Integer.valueOf(rs.getString("length")) : null);
            columnInfo.setPrecision(rs.getString("numeric_precision") != null ? Integer.valueOf(rs.getString("numeric_precision")) : null );
            columnInfo.setScale(rs.getString("numeric_scale") != null ? Integer.valueOf(rs.getString("numeric_scale")) : null);
        }

        return result;
    }
}
