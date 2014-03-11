package ru.intertrust.cm.core.dao.impl;

import java.util.List;

import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Класс для генерации sql запросов для {@link ru.intertrust.cm.core.dao.impl.PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public class OracleQueryHelper extends BasicQueryHelper {

    @Override
    public String generateCountTablesQuery() {
        return "select count(table_name) FROM user_tables";
    }

    @Override
    protected String getIdType() {
        return "number(19)";
    }

    @Override
    protected String getTextType() {
        return "clob";
    }

    @Override
    protected String generateIndexQuery(String tableName, String indexType, List<String> fieldNames) {
        String indexFieldsPart = createIndexTableFieldsPart(fieldNames);

        String indexName = createExplicitIndexName(tableName, fieldNames);
        return "create index " + wrap(indexName) + " on " + wrap(tableName) + " (" + indexFieldsPart + ")";
    }
}
