package ru.intertrust.cm.remoteclient.permissions.test;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.remoteclient.ClientBase;

import javax.naming.NamingException;
import java.util.Collections;

public class TestAdministratorsPermissions extends ClientBase {
    public static void main(String[] args) {
        try {
            TestAdministratorsPermissions test = new TestAdministratorsPermissions();
            test.execute(args);
        } catch (Exception ex) {
            logger.error("Error execute test", ex);
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            CrudService crudService = getCrudService("administrator", "administrator");
            CollectionsService collectionsService = getCollectionService("administrator", "administrator");
            DomainObject doc1 = crudService.createDomainObject("test_outgoing_document");
            doc1 = crudService.save(doc1);

            doc1 = crudService.find(doc1.getId());
            assertTrue("find test_outgoing_document", doc1 != null);

            String query = "select id from test_outgoing_document where id = {0}";
            IdentifiableObjectCollection col = collectionsService.findCollectionByQuery(
                    query, Collections.singletonList(new ReferenceValue(doc1.getId())));
            assertTrue("query test_outgoing_document", col.size() > 0);


            query = "select id from test_document_base where id = {0}";
            col = collectionsService.findCollectionByQuery(
                    query, Collections.singletonList(new ReferenceValue(doc1.getId())));
            assertTrue("query test_document_base", col.size() > 0);

            if (hasError){
                log("Test error");
            }else{
                log("Test OK");
            }
        } finally {
            writeLog();
        }
    }

    private CrudService.Remote getCrudService(String login, String password) throws NamingException {
        return (CrudService.Remote) getService("CrudServiceImpl", CrudService.Remote.class, login, password);
    }

    private CollectionsService.Remote getCollectionService(String login, String password) throws NamingException{
        return (CollectionsService.Remote) getService("CollectionsServiceImpl", CollectionsService.Remote.class, login, password);
    }
}
