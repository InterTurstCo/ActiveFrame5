package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.config.BaseIndexExpressionConfig;
import ru.intertrust.cm.core.config.DateTimeWithTimeZoneFieldConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.MD5Service;

import java.util.Collections;
import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.*;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

/**
 * Класс для генерации sql запросов для {@link PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public class PostgreSqlQueryHelper extends BasicQueryHelper {

    private static final String FOREIGN_KEYS_QUERY =
            "select conname constraint_name, conrelid::regclass table_name, a.attname column_name, confrelid::regclass " +
                    "foreign_table_name, af.attname foreign_column_name " +
                    "from (select conname, conrelid, confrelid, conkey[i] as conkey, confkey[i] as confkey " +
                            "from (select conname, conrelid, confrelid, conkey, confkey, generate_series(1, array_upper(conkey, 1)) as i " +
                                "from pg_constraint join pg_tables on pg_constraint.confrelid::regclass::varchar = pg_tables.tablename " +
                                "where pg_tables.schemaname !~ '^pg_' and pg_tables.schemaname != 'information_schema' and contype = 'f') " +
                                "c) " +
                            "c " +
                    "join pg_attribute a on c.conrelid = a.attrelid and c.conkey = a.attnum " +
                    "join pg_attribute af on c.confrelid = af.attrelid and c.confkey = af.attnum " +
                    "order by c.conname";

    private static final String UNIQUE_KEYS_QUERY =
            "select conname constraint_name, conrelid::regclass table_name, a.attname column_name " +
                "from (select conname, conrelid, confrelid, unnest(conkey) as column_index from pg_constraint " +
                        "where contype='u') c " +
                "join pg_attribute a on c.conrelid = a.attrelid and c.column_index = a.attnum " +
                "join pg_tables t on c.conrelid::regclass::varchar = t.tablename " +
                "where t.schemaname !~ '^pg_' and t.schemaname != 'information_schema' order by c.conname";

    private static final String COLUMNS_QUERY =
            "select table_name, column_name, data_type, is_nullable nullable, character_maximum_length length, " +
                    "numeric_precision, numeric_scale " +
                    "from information_schema.columns where table_schema = 'public'";

    private static final String INDEXES_QUERY =
            "select table_class.relname as table_name, index_class.relname as index_name, attr.attname as column_name " +
                    "from pg_index join pg_class table_class on table_class.oid = pg_index.indrelid " +
                        "join pg_class index_class on index_class.oid = pg_index.indexrelid " +
                        "left join pg_attribute attr on (attr.attrelid = table_class.oid and attr.attnum = ANY(pg_index.indkey)) " +
                        "join pg_tables tables on table_class.relname = tables.tablename " +
                    "where table_class.relkind = 'r' and " +
                        "index_class.relname like 'i%' and " +
                        "tables.schemaname !~ '^pg_' and tables.schemaname != 'information_schema' " +
                    "order by table_class.relname, index_class.relname;";

    private static final String INDEXES_QUERY_BY_TABLE =
            "select table_class.relname as table_name, index_class.relname as index_name, attr.attname as column_name " +
                    "from pg_index join pg_class table_class on table_class.oid = pg_index.indrelid " +
                        "join pg_class index_class on index_class.oid = pg_index.indexrelid " +
                        "left join pg_attribute attr on (attr.attrelid = table_class.oid and attr.attnum = ANY(pg_index.indkey)) " +
                        "join pg_tables tables on table_class.relname = tables.tablename " +
                    "where table_class.relkind = 'r' and " +
                        "index_class.relname like 'i%' and " +
                        "tables.schemaname !~ '^pg_' and tables.schemaname != 'information_schema' " +
                        "and lower(table_class.relname) = ? " +
                    "order by table_class.relname, index_class.relname;";

    private static final String STATISTICS_QUERY = "analyze";

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
    protected String getDecimalType() {
        return "numeric";
    }

    @Override
    protected String generateIndexQuery(DomainObjectTypeConfig config, String indexType, List<String> indexFields, List<String> indexExpressions) {
        String indexFieldsPart = createIndexFieldsPart(indexFields, indexExpressions);
        // имя индекса формируется из MD5 хеша DDL выражения
        String indexName = generateExplicitIndexName(config, indexFields, indexExpressions);
        return "create index " + wrap(indexName) + " on " + wrap(getSqlName(config)) +
                " USING " + indexType + " (" + indexFieldsPart + ")";
    }

    @Override
     public String generateSetColumnNotNullQuery(DomainObjectTypeConfig config, FieldConfig fieldConfig, boolean notNull) {
        StringBuilder query = new StringBuilder();
        query.append("alter table ").append(wrap(getSqlName(config))).append(" alter column ").
                append(wrap(getSqlName(fieldConfig)));
        if (notNull) {
            query.append(" set not null;");
        } else {
            query.append(" drop not null;");
        }

        if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            query.append("alter table ").append(wrap(getSqlName(config))).append(" alter column ").
                    append(wrap(getReferenceTypeColumnName(fieldConfig.getName())));
            if (notNull) {
                query.append(" set not null;");
            } else {
                query.append(" drop not null;");
            }
        } else if (DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
            query.append("alter table ").append(wrap(getSqlName(config))).append(" alter column ").
                    append(wrap(getTimeZoneIdColumnName(fieldConfig.getName())));
            if (notNull) {
                query.append(" set not null;");
            } else {
                query.append(" drop not null;");
            }
        }

        return query.toString();
    }



    @Override
    public List<String> generateUpdateColumnTypeQueries(DomainObjectTypeConfig config, FieldConfig fieldConfig) {
        StringBuilder query = new StringBuilder();
        final String wrappedColumnName = wrap(getSqlName(fieldConfig));
        final String sqlType = getSqlType(fieldConfig);
        query.append("alter table ").append(wrap(getSqlName(config))).append(" alter column ").
                append(wrappedColumnName).append(" set data type ").append(sqlType).append(";");
        if (!isAuditLog(config)) {
            query.append("alter table ").append(wrap(getALTableSqlName(config.getName()))).append(" alter column ").
                    append(wrappedColumnName).append(" set data type ").append(sqlType).append(";");
        }

        return Collections.singletonList(query.toString());
    }

    /**
     * Метод возвращает true если тип является описание аудит лога
     * @param config
     * @return
     */
    private boolean isAuditLog(DomainObjectTypeConfig config) {
        return config.getName().toLowerCase().endsWith(Configuration.AUDIT_LOG_SUFFIX);
    }

    @Override
    public String generateGatherStatisticsQuery() {
        return STATISTICS_QUERY;
    }

    @Override
    public String generateGetForeignKeysQuery() {
        return FOREIGN_KEYS_QUERY;
    }

    @Override
    public String generateGetUniqueKeysQuery() {
        return UNIQUE_KEYS_QUERY;
    }

    @Override
    public String generateGetIndexesQuery() {
        return INDEXES_QUERY;
    }

    @Override
    public String generateGetIndexesByTableQuery() {
        return INDEXES_QUERY_BY_TABLE;
    }

    @Override
    protected String generateIsTableExistQuery() {
        return "select count(*) FROM information_schema.tables WHERE table_schema = 'public' and table_name = ?";
    }

    @Override
    protected String generateGetSchemaTablesQuery() {
        return COLUMNS_QUERY;
    }

    @Override
    public String getSqlIndexExpression(BaseIndexExpressionConfig indexFieldConfig) {
        return DataStructureNamingHelper.getQuoteCaseSensitiveIndexExpr(indexFieldConfig);
    }

}
