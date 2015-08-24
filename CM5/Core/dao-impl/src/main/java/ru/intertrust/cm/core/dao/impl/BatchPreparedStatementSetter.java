package ru.intertrust.cm.core.dao.impl;

import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация org.springframework.jdbc.core.ParameterizedPreparedStatementSetter, используется для установки параметров
 * PreparedStatement при batch-операциях сохранения и изменения
 */
public class BatchPreparedStatementSetter implements org.springframework.jdbc.core.ParameterizedPreparedStatementSetter<Map<String, Object>> {

    private Map<String, List<Integer>> parameterIndexMap;

    public BatchPreparedStatementSetter(Map<String, List<Integer>> parameterIndexMap) {
        if (parameterIndexMap != null) {
            this.parameterIndexMap = parameterIndexMap;
        } else {
            this.parameterIndexMap = new HashMap<>();
        }
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
            List<Integer> indexes = parameterIndexMap.get(entry.getKey());
            if (indexes != null) {
                for (Integer index : indexes) {
                    StatementCreatorUtils.setParameterValue(ps, index, SqlTypeValue.TYPE_UNKNOWN, entry.getValue());
                }
            }
        }
    }
}