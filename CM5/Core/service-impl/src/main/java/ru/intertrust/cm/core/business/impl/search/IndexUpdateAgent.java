package ru.intertrust.cm.core.business.impl.search;

import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint
public class IndexUpdateAgent implements AfterSaveExtensionHandler {

    @Autowired
    private SolrServer solrServer;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        // TODO Auto-generated method stub

    }

}
