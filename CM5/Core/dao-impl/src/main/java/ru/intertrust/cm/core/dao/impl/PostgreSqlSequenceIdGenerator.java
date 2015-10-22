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

    @Override
    protected String generateSelectNextValuesQuery(String sequenceName, Integer nextValuesNumber) {
        if (nextValuesNumber == null || nextValuesNumber < 1) {
            throw new IllegalArgumentException("nextValuesNumber must be positive integer");
        }

        StringBuilder query = new StringBuilder();
        query.append("select nextval ('");
        query.append(sequenceName);
        query.append("') from generate_series(1," + nextValuesNumber + ")");

        return query.toString();
    }
}
