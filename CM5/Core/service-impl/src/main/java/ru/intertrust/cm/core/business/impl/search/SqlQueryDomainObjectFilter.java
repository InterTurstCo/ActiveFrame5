package ru.intertrust.cm.core.business.impl.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.DomainObjectFilter;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

/**
 * Возвращает результат вычисления SQL запроса в фильтре в области поиска. 
 * @author atsvetkov
 *
 */
public class SqlQueryDomainObjectFilter implements DomainObjectFilter {

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations jdbcTemplate;

    private String sqlQuery;

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    @Override
    public boolean filter(DomainObject object) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        long id = ((RdbmsId) object.getId()).getId();
        int idType = ((RdbmsId) object.getId()).getTypeId();

        paramMap.put("id", id);
        paramMap.put("id_type", idType);
        StringBuilder sqlQueryBuilder = new StringBuilder(sqlQuery);
        sqlQueryBuilder.append(" and id_type=:id_type ");
        sqlQuery = sqlQueryBuilder.toString();

        List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(sqlQuery, paramMap);
        return (queryResult != null && queryResult.size() > 0);
    }

}
