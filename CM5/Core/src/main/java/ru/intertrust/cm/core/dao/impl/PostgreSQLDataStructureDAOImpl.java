package ru.intertrust.cm.core.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.config.*;

import javax.sql.DataSource;
import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlName;

/**
 * @author vmatsukevich
 *         Date: 5/15/13
 *         Time: 4:27 PM
 */
public class PostgreSQLDataStructureDAOImpl extends AbstractDataStructureDAOImpl {

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void createTable(BusinessObjectConfig config) {
        String query = "create table " + getSqlName(config) + " ( " + "" +
                "ID bigint not null, " +
                "CREATE_DATE timestamp not null, " +
                "MODIFY_DATE timestamp not null";

        for(FieldConfig fieldConfig : config.getFieldConfigs()) {
            query += ", " + getSqlName(fieldConfig) + " " + getSqlType(fieldConfig);
            if(fieldConfig.isNotNull()) {
                query += " not null";
            }
        }

        query += ", constraint primary key PK_ID(ID)";

        for(UniqueKey uniqueKey : config.getUniqueKeys()) {
            if(!uniqueKey.getFields().isEmpty()) {
                query += ", constraint unique UNQ_" + getFieldsListAsString(uniqueKey.getFields(), "_") + "(";
                query += getFieldsListAsString(uniqueKey.getFields(), ", ") + ")";
            }
        }

        for(FieldConfig fieldConfig : config.getFieldConfigs()) {
            if(!ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
                continue;
            }

            ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
            String fieldSqlName = getSqlName(referenceFieldConfig);

            query += ", constraint foreign key FK_" + fieldSqlName + "(" + fieldSqlName + ") references " +
                    getSqlName(referenceFieldConfig) + "(ID)";
        }

    }

    @Override
    public Integer countTables() {
        String query = "select count(table_name) FROM information_schema.tables WHERE table_schema = 'public'";
        return jdbcTemplate.queryForObject(query, Integer.class);
    }

    private String getSqlType(FieldConfig fieldConfig) {
        if(DateTimeFieldConfig.class.equals(fieldConfig.getClass())) {
            return "timestamp";
        }

        if(DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
            return "decimal";
        }

        if(LongFieldConfig.class.equals(fieldConfig.getClass())) {
            return "bigint";
        }

        if(ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            return "bigint";
        }

        if(StringFieldConfig.class.equals(fieldConfig.getClass())) {
            return "varchar(" + ((StringFieldConfig) fieldConfig).getLength() + ")";
        }

        if(PasswordFieldConfig.class.equals(fieldConfig.getClass())) {
            return "varchar(" + ((PasswordFieldConfig) fieldConfig).getLength() + ")";
        }

        throw new IllegalArgumentException("Invalid field type");
    }

    private String getFieldsListAsString(List<Field> fieldList, String delimiter) {
        if(fieldList.isEmpty()) {
            throw new IllegalArgumentException("Field list is empty");
        }

        String result = "";
        for(int i = 0; i < fieldList.size(); i++) {
            if(i > 0) {
                result += delimiter;
            }
            result += fieldList.get(i).getName();
        }

        return result;
    }
}
