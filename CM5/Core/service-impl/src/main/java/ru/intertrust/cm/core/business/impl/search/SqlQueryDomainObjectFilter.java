package ru.intertrust.cm.core.business.impl.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.DomainObjectFilter;
import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * Возвращает результат вычисления SQL запроса в фильтре в области поиска. 
 * @author atsvetkov
 *
 */
public class SqlQueryDomainObjectFilter implements DomainObjectFilter {

    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

    private String sqlQuery;

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    @Override
    public boolean filter(DomainObject object) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", object.getId());
        List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(sqlQuery, paramMap);
        return (queryResult != null && queryResult.size() > 0);
    }

}
