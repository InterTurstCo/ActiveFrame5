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
public class SequenceIdGenerator implements IdGenerator {

    @Autowired
    private JdbcOperations jdbcTemplate;

    public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Object generatetId(DomainObjectTypeConfig domainObjectTypeConfig) {
        return generateIdFromSequence(DataStructureNamingHelper.getSqlSequenceName(domainObjectTypeConfig));
    }

    @Override
    public Object generatetLogId(DomainObjectTypeConfig domainObjectTypeConfig) {
        return generateIdFromSequence(DataStructureNamingHelper.getSqlAuditSequenceName(domainObjectTypeConfig));
    }

    @Override
    public Object generateId(String name) {
        return generateIdFromSequence(getSqlSequenceName(name));
    }

    public Object generateIdFromSequence(String sequenceName) {
        StringBuilder query = new StringBuilder();
        query.append("select nextval ('");
        query.append(sequenceName);
        query.append("')");
        return jdbcTemplate.queryForObject(query.toString(), Long.class);
    }

}
