package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.IdGenerator;

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

        String sequenceName = DataStructureNamingHelper.getSqlSequenceName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("select nextval ('");
        query.append(sequenceName);
        query.append("')");
        return jdbcTemplate.queryForObject(query.toString(), Long.class);
    }

    @Override
    public Object generatetLogId(DomainObjectTypeConfig domainObjectTypeConfig) {

        String sequenceName = DataStructureNamingHelper.getSqlAuditSequenceName(domainObjectTypeConfig);

        StringBuilder query = new StringBuilder();
        query.append("select nextval ('");
        query.append(sequenceName);
        query.append("')");
        return jdbcTemplate.queryForObject(query.toString(), Long.class);
    }


}
