package ru.intertrust.cm.core.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;
import ru.intertrust.cm.core.dao.api.IdGenerator;

import javax.sql.DataSource;

/**
 * Создает(генерирует) уникальный идентификатор используя последовательность(сиквенс)  в базе данных
 * @author skashanski
 *
 */
public class SequenceIdGenerator implements IdGenerator {


    private  JdbcTemplate jdbcTemplate;



    /**
     * Устанавливает источник соединений
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
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
