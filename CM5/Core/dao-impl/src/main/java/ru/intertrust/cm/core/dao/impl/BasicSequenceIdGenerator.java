package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import ru.intertrust.cm.core.dao.api.IdGenerator;

import java.util.List;

import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlSequenceName;

/**
 * Создает(генерирует) уникальный идентификатор используя последовательность(сиквенс)  в базе данных
 * @author skashanski
 *
 */
public abstract class BasicSequenceIdGenerator implements IdGenerator {

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcOperations jdbcTemplate;

    public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Object generateId(Integer doTypeId) {
        return generateIdFromSequence(DataStructureNamingHelper.getSqlSequenceName(doTypeId));
    }

    @Override
    public List generateIds(Integer doTypeId, Integer idsNumber) {
        return generateIdsFromSequence(DataStructureNamingHelper.getSqlSequenceName(doTypeId), idsNumber);
    }

    @Override
    public Object generateAuditLogId(Integer doTypeId) {
        return generateIdFromSequence(DataStructureNamingHelper.getSqlAuditSequenceName(doTypeId));
    }

    @Override
    public Object generateId(String name) {
        return generateIdFromSequence(getSqlSequenceName(name));
    }

    protected Object generateIdFromSequence(String sequenceName) {
        return jdbcTemplate.queryForObject(generateSelectNextValueQuery(sequenceName), Long.class);
    }

    protected List generateIdsFromSequence(String sequenceName, Integer idsNumber) {
        return jdbcTemplate.queryForList(generateSelectNextValuesQuery(sequenceName, idsNumber), Long.class);
    }

    protected abstract String generateSelectNextValueQuery(String sequenceName);

    protected abstract String generateSelectNextValuesQuery(String sequenceName, Integer nextValuesNumber);

}
