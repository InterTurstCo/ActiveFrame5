package ru.intertrust.cm.core.business.impl.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.DomainObjectFilter;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

/**
 * Возвращает результат вычисления SQL запроса в фильтре в области поиска. 
 * @author atsvetkov
 *
 */
public class SqlQueryDomainObjectFilter implements DomainObjectFilter {

    @Autowired
    private CollectionsService collectionsService;

    private String sqlQuery;

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    @Override
    public boolean filter(DomainObject object) {
        IdentifiableObjectCollection result = collectionsService.findCollectionByQuery(sqlQuery,
                Collections.singletonList(new ReferenceValue(object.getId())), 0, 1);
        return (result != null && result.size() > 0);
    }

}
