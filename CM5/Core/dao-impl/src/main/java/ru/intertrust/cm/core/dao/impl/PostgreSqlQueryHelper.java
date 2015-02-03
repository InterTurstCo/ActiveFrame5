package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

import java.util.List;

import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.MD5Service;

/**
 * Класс для генерации sql запросов для {@link PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public class PostgreSqlQueryHelper extends BasicQueryHelper {

    private static final String FOREIGN_KEYS_QUERY =
            "select constr.constraint_name, column_usage.table_name, column_usage.column_name, " +
                    "column_usage2.table_name foreign_table_name, column_usage2.column_name foreign_column_name " +
                    "from information_schema.referential_constraints constr " +
                    "join information_schema.key_column_usage column_usage " +
                        "on column_usage.constraint_name = constr.constraint_name " +
                    "join information_schema.key_column_usage column_usage2 " +
                    "    on column_usage2.ordinal_position = column_usage.position_in_unique_constraint " +
                    "    and column_usage2.constraint_name = constr.unique_constraint_name " +
                    "where column_usage.table_schema = 'public'";

    private static final String COLUMNS_QUERY =
            "select table_name, column_name, is_nullable nullable, character_maximum_length length, " +
                    "numeric_precision, numeric_scale " +
                    "from information_schema.columns where table_schema = 'public'";

    protected PostgreSqlQueryHelper(DomainObjectTypeIdDao DomainObjectTypeIdDao, MD5Service md5Service) {
        super(DomainObjectTypeIdDao, md5Service);
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
    protected String generateIndexQuery(DomainObjectTypeConfig config, String indexType, List<String> indexFields, List<String> indexExpressions) {
        String indexFieldsPart = createIndexFieldsPart(indexFields, indexExpressions);
        // имя индекса формируется из MD5 хеша DDL выражения
        String indexName = createExplicitIndexName(config, indexFields, indexExpressions);
        return "create index " + wrap(indexName) + " on " + wrap(getSqlName(config)) +
                " USING " + indexType + " (" + indexFieldsPart + ")";
    }
}
