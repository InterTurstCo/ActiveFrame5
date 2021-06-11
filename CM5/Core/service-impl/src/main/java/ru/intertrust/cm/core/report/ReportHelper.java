package ru.intertrust.cm.core.report;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;

/**
 * Служебный класс для работы с отчетами
 *
 * @author larin
 */
public class ReportHelper {

    @Autowired
    protected CollectionsService collectionsService;

    @Autowired
    protected CrudService crudService;

    /**
     * Получение доменного объекта отчета по имени
     */
    public DomainObject getReportTemplateObject(String name) {
        String query = "select t.id from report_template t where t.name = '" + name + '\'';
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query, 0, 1, DataSourceContext.CLONE);
        DomainObject result = null;
        if (collection.size() > 0) {
            IdentifiableObject row = collection.get(0);
            result = crudService.find(row.getId(), DataSourceContext.CLONE);
        }
        return result;
    }
}
