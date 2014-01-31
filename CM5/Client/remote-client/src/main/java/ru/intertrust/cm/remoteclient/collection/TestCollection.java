package ru.intertrust.cm.remoteclient.collection;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestCollection extends ClientBase {

    private CollectionsService.Remote collectionService;

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

            query =
                    "select x.id, x.login, x.organization from (select t.id, t.login, d.organization from person t inner join department d on (d.boss = t.id)) x";
            executeQuery(query, 3);

            query =
                    "select CASE WHEN d.parentdepartment is null THEN d.id ELSE d.parentdepartment END as id, CASE WHEN d.organization is null THEN d.id ELSE d.organization END as org_id from department d";
            executeQuery(query, 2);

            query = "select d.id from department d where d.id = {0}";
            List<Value> params = new ArrayList<Value>();
            params.add(new ReferenceValue(new RdbmsId(1, 1)));
            executeQuery(query, 1, params);

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
        executeQuery(query, columnCount, null);
    }

    private void executeQuery(String query, int columnCount, List<Value> params) throws Exception {
        IdentifiableObjectCollection collection = null;
        if (params == null) {
            collection =
                    collectionService.findCollectionByQuery(query);
        } else {
            collection =
                    collectionService.findCollectionByQuery(query, params);
        }

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

    private void executeCollection(String collectionName, int columnCount, List<Filter> filters) throws Exception {
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
