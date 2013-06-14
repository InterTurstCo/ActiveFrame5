package ru.intertrust.cm.core.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.intertrust.cm.core.config.model.BusinessObjectConfig;
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
    public Object generatetId(BusinessObjectConfig businessObjectConfig) {

        String sequenceName = DataStructureNamingHelper.getSqlSequenceName(businessObjectConfig);

        StringBuilder query = new StringBuilder();
        query.append("select nextval ('");
        query.append(sequenceName);
        query.append("')");
        return jdbcTemplate.queryForObject(query.toString(), Long.class);


    }



}
