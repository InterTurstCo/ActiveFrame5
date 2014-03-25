package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;

import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Класс для генерации sql запросов для {@link ru.intertrust.cm.core.dao.impl.PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public class OracleQueryHelper extends BasicQueryHelper {

    protected OracleQueryHelper(DomainObjectTypeIdDao domainObjectTypeIdDao) {
        super(domainObjectTypeIdDao);
    }

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
    protected String generateIndexQuery(DomainObjectTypeConfig config, String indexType, List<String> fieldNames, int index) {
        String indexFieldsPart = createIndexTableFieldsPart(fieldNames);

        String indexName = createExplicitIndexName(config, index, false);
        return "create index " + wrap(indexName) + " on " + wrap(getSqlName(config)) + " (" + indexFieldsPart + ")";
    }
}
