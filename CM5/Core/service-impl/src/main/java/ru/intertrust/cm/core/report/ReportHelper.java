package ru.intertrust.cm.core.report;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionsDao;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.model.ReportServiceException;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Служебный класс для работы с отчетами
 * @author larin
 * 
 */
public class ReportHelper {

    @Autowired
    protected CollectionsService collectionsService;

    @Autowired
    protected CrudService crudService;

    /**
     * Получение доменного объекта отчета по имени
     * @param name
     * @return
     */
    public DomainObject getReportTemplateObject(String name) {
        String query = "select t.id from report_template t where t.name = '" + name + "'";
        IdentifiableObjectCollection collection = collectionsService.findCollectionByQuery(query, 0, 1, DataSourceContext.CLONE);
        DomainObject result = null;
        if (collection.size() > 0) {
            IdentifiableObject row = collection.get(0);
            result = crudService.find(row.getId(), DataSourceContext.CLONE);
        }
        return result;
    }
}
