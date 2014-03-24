package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.IdGenerator;

import static ru.intertrust.cm.core.dao.api.DomainObjectTypeIdDao.DOMAIN_OBJECT_TYPE_ID_TABLE;
import static ru.intertrust.cm.core.dao.impl.DataStructureNamingHelper.getSqlSequenceName;

/**
 * Создает(генерирует) уникальный идентификатор используя последовательность(сиквенс)  в базе данных
 * @author skashanski
 *
 */
public abstract class BasicSequenceIdGenerator implements IdGenerator {

    @Autowired
    private JdbcOperations jdbcTemplate;

    public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Object generateId(Integer doTypeId) {
        return generateIdFromSequence(DataStructureNamingHelper.getSqlSequenceName(doTypeId));
    }

    @Override
    public Object generatetLogId(Integer doTypeId) {
        return generateIdFromSequence(DataStructureNamingHelper.getSqlAuditSequenceName(doTypeId));
    }

    @Override
    public Object generateId(String name) {
        return generateIdFromSequence(getSqlSequenceName(name));
    }

    public Object generateIdFromSequence(String sequenceName) {
        return jdbcTemplate.queryForObject(generateSelectNextValueQuery(sequenceName), Long.class);
    }

    protected abstract String generateSelectNextValueQuery(String sequenceName);

}
