package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.intertrust.cm.core.business.api.dto.IndexInfo;
import ru.intertrust.cm.core.business.api.dto.UniqueKeyInfo;
import ru.intertrust.cm.core.config.IndexConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс-маппер для извлечения данных индексах из ResultSet
 * Created by vmatsukevich on 27.1.15.
 */
public class IndexesRowMapper implements ResultSetExtractor<Map<String, Map<String, IndexInfo>>> {

    /**
     * Ихвлекает данные о индексах из ResultSet
     * @param rs
     * @return
     * @throws java.sql.SQLException
     * @throws org.springframework.dao.DataAccessException
     */
    @Override
    public Map<String, Map<String, IndexInfo>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, Map<String, IndexInfo>> result = new HashMap<>();

        while (rs.next()) {
            String tableName = rs.getString("table_name");
            Map<String, IndexInfo> indexes = result.get(tableName);

            if (indexes == null) {
                indexes = new HashMap<>();
                result.put(tableName, indexes);
            }

            String indexName = rs.getString("index_name");
            IndexInfo indexInfo = indexes.get(indexName);

            if (indexInfo == null) {
                indexInfo = new IndexInfo();
                indexes.put(indexName, indexInfo);

                indexInfo.setName(indexName);
                indexInfo.setTableName(tableName);
            }

            if (rs.getString("column_name") != null) {
                indexInfo.getColumnNames().add(rs.getString("column_name"));
            }
        }

        return result;
    }
}
