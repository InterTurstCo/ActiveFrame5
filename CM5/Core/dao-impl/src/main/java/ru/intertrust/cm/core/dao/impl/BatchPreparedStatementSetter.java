package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.dao.impl.utils.ParameterType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;

/**
 * Реализация org.springframework.jdbc.core.ParameterizedPreparedStatementSetter, используется для установки параметров
 * PreparedStatement при batch-операциях сохранения и изменения
 */
public class BatchPreparedStatementSetter implements org.springframework.jdbc.core.ParameterizedPreparedStatementSetter<Map<String, Object>> {

    private Query query;

    public BatchPreparedStatementSetter(Query query) {
        this.query = query;
    }

    /**
     * Set parameter values on the given PreparedStatement.
     *
     * @param ps       the PreparedStatement to invoke setter methods on
     * @param argument the object containing the values to be set
     * @throws java.sql.SQLException if a SQLException is encountered (i.e. there is no need to catch SQLException)
     */
    @Override
    public void setValues(PreparedStatement ps, Map<String, Object> argument) throws SQLException {
        if (argument == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : argument.entrySet()) {
            Query.ParameterInfo parameterInfo = query.getParameterInfoMap().get(entry.getKey());
            if (parameterInfo == null) {
                continue;
            }

            Integer index = parameterInfo.getIndex();
            if (index == null) {
                continue;
            }

            if (entry.getValue() == null) {
                ps.setNull(index, parameterInfo.getType().getSqlType());
            } else {
                if (ParameterType.DATE == parameterInfo.getType()){
                    Calendar cal = (Calendar) entry.getValue();
                    ps.setTimestamp(index, new java.sql.Timestamp(cal.getTime().getTime()), cal);
                } else {
                    ps.setObject(index, entry.getValue(), parameterInfo.getType().getSqlType());
                }
            }
        }
    }
}