package ru.intertrust.cm.core.dao.impl;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getReferenceTypeColumnName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getTimeZoneIdColumnName;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.wrap;

import java.util.List;

import ru.intertrust.cm.core.config.DateTimeWithTimeZoneFieldConfig;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.ReferenceFieldConfig;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao;
import ru.intertrust.cm.core.dao.api.MD5Service;

/**
 * Класс для генерации sql запросов для {@link ru.intertrust.cm.core.dao.impl.PostgreSqlDataStructureDaoImpl}
 * @author vmatsukevich Date: 5/20/13 Time: 2:12 PM
 */
public class OracleQueryHelper extends BasicQueryHelper {

    private static final String FOREIGN_KEYS_QUERY =
            "select columns.constraint_name, columns.table_name, columns.column_name, " +
                    "constraints2.table_name foreign_table_name, constraints2.constraint_name foreign_column_name " +
            "from all_cons_columns columns " +
            "join all_constraints constraints on columns.owner = constraints.owner " +
                "and columns.constraint_name = constraints.constraint_name " +
            "join all_constraints constraints2 on constraints.r_owner = constraints2.owner " +
                "and constraints.r_constraint_name = constraints2.constraint_name " +
            "where constraints.constraint_type = 'R'";

    private static final String COLUMNS_QUERY =
            "select table_name, column_name, nullable, char_length length, data_precision numeric_precision, " +
                    "data_scale numeric_precision from user_tab_columns";

    protected OracleQueryHelper(DomainObjectTypeIdDao domainObjectTypeIdDao, MD5Service md5Service) {
        super(domainObjectTypeIdDao, md5Service);
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
    protected String generateIndexQuery(DomainObjectTypeConfig config, String indexType, List<String> indexFields, List<String> indexExpressions) {
        String indexFieldsPart = createIndexFieldsPart(indexFields, indexExpressions);
        // имя индекса формируется из MD5 хеша DDL выражения
        String indexName = createExplicitIndexName(config, indexFields, indexExpressions);
        return "create index " + wrap(indexName) + " on " + wrap(getSqlName(config)) + " (" + indexFieldsPart + ")";
    }

    @Override
    public String generateSetColumnNullableQuery(DomainObjectTypeConfig config, FieldConfig fieldConfig) {
        StringBuilder query = new StringBuilder();
        query.append("alter table ").append(wrap(getSqlName(config))).append(" modify column ").
                append(wrap(getSqlName(fieldConfig))).append(" ").append(getSqlType(fieldConfig)).append(" null;");

        if (ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            query.append("alter table ").append(wrap(getSqlName(config))).append(" modify column ").
                    append(wrap(getReferenceTypeColumnName(fieldConfig.getName()))).append(" ").
                    append(getReferenceTypeSqlType()).append(" null;");
        } else if (DateTimeWithTimeZoneFieldConfig.class.equals(fieldConfig.getClass())) {
            query.append("alter table ").append(wrap(getSqlName(config))).append(" modify column ").
                    append(wrap(getTimeZoneIdColumnName(fieldConfig.getName()))).append(" ").
                    append(getTimeZoneIdSqlType()).append(" null;");
        }

        return query.toString();
    }

    @Override
    protected String generateIsTableExistQuery() {
        return "select count(*) FROM user_tables WHERE table_name = ?";
    }

    @Override
    protected String generateGetSchemaTablesQuery() {
        return COLUMNS_QUERY;
    }

    @Override
    protected String generateGetForeignKeysQuery() {
        return FOREIGN_KEYS_QUERY;
    }

}
