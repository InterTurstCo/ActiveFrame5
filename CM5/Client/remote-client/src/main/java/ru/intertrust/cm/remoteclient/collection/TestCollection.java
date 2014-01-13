package ru.intertrust.cm.remoteclient.collection;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestCollection extends ClientBase {
    private CrudService.Remote crudService;

    private CollectionsService.Remote collectionService;

    private AttachmentService.Remote attachmentService;

    public static void main(String[] args) {
        try {
            TestCollection test = new TestCollection();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        try {
            super.execute(args);

            collectionService = (CollectionsService.Remote) getService(
                    "CollectionsServiceImpl", CollectionsService.Remote.class);

            String query = "select t.id, t.login, 'xxx' as xxx, 'yyy' as yyy from person t";
            executeQuery(query, 4);
            
            query = "select t.id, t.organization from department t ";
            query += "union ";
            query += "select t.id, null as organization, null as organization_type from department t ";
            executeQuery(query, 2);

            
            query = "select x.id, x.col2 from ( ";
            query += "select t.id, t.created_date as col2, t.organization as col3 from department t ";
            query += "union ";
            query += "select t.id, t.created_date as col2, t.organization as col3 from department t ";
            query += ") x";
            executeQuery(query, 2);

            query = "select x.id, x.col2, x.col3 from ( ";
            query += "select t.id, t.created_date as col2, t.organization as col3 from department t ";
            query += "union ";
            query += "select t.id, t.created_date as col2, t.organization as col3 from department t ";
            query += ") x";
            executeQuery(query, 3);
            
            //TODO расскомментировать после исполнения 395
            /*List<Filter> filters = new ArrayList<Filter>();
            Filter filter = new Filter();
            filter.setFilter("byLastName");
            filter.addCriterion(0, null);
            filters.add(filter);
            executeCollection("PersonTestNull", 1, filters);*/
            
        } finally {
            writeLog();
        }
    }

    private void executeQuery(String query, int columnCount) throws Exception {
        IdentifiableObjectCollection collection =
                collectionService.findCollectionByQuery(query);
        
        assertTrue("Column count", collection.getFieldsConfiguration().size() == columnCount);
        
        for (FieldConfig config : collection.getFieldsConfiguration()) {
            System.out.print("\t" + config.getName());
        }
        System.out.println("");
        for (IdentifiableObject row : collection) {
            for (FieldConfig config : collection.getFieldsConfiguration()) {
                System.out.print("\t" + row.getValue(config.getName()));
            }
            System.out.println("");
        }
        log("Test query " + query + " OK");
    }

    private void executeCollection(String collectionName, int columnCount, List<Filter>filters) throws Exception {
        IdentifiableObjectCollection collection =
                collectionService.findCollection(collectionName, null, filters);
        
        assertTrue("Column count", collection.getFieldsConfiguration().size() == columnCount);
        
        for (FieldConfig config : collection.getFieldsConfiguration()) {
            System.out.print("\t" + config.getName());
        }
        System.out.println("");
        for (IdentifiableObject row : collection) {
            for (FieldConfig config : collection.getFieldsConfiguration()) {
                System.out.print("\t" + row.getValue(config.getName()));
            }
            System.out.println("");
        }
        log("Test query " + collectionName + " OK");
    }

}
