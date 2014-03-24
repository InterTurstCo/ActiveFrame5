package ru.intertrust.cm.core.dao.impl;

/**
 * PostgreSql-специфичная имплементация {@link ru.intertrust.cm.core.dao.api.IdGenerator }
 * @author vmatsukevich
 *
 */
public class PostgreSqlSequenceIdGenerator extends BasicSequenceIdGenerator {

    @Override
    protected String generateSelectNextValueQuery(String sequenceName) {
        StringBuilder query = new StringBuilder();
        query.append("select nextval ('");
        query.append(sequenceName);
        query.append("')");

        return query.toString();
    }
}
