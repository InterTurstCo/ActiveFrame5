package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Класс для генерации sql запросов для {@link PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public class PostgreSqlQueryHelper extends BasicQueryHelper {

    protected PostgreSqlQueryHelper(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        super(domainObjectTypeIdCache);
    }

    @Override
    public String generateCountTablesQuery() {
        return "select count(table_name) FROM information_schema.tables WHERE table_schema = 'public'";
    }

    @Override
    protected String getIdType() {
        return "bigint";
    }

    @Override
    protected String getTextType() {
        return "text";
    }

    @Override
    protected String generateIndexQuery(DomainObjectTypeConfig config, String indexType, List<String> fieldNames, int index) {
        String indexFieldsPart = createIndexTableFieldsPart(fieldNames);
        String indexName = createExplicitIndexName(config, index, false);
        return "create index " + wrap(indexName) + " on " + wrap(getSqlName(config)) +
                " USING " + indexType + " (" + indexFieldsPart + ")";
    }
}
