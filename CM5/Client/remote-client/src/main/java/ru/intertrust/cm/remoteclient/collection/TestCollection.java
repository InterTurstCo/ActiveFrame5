package ru.intertrust.cm.remoteclient.collection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.SearchService;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestCollection extends ClientBase {

    private CollectionsService.Remote collectionService;
    
    private SearchService.Remote searchService;

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
            
            searchService = (SearchService.Remote) getService(
                    "SearchService", SearchService.Remote.class);
            //searchService.dumpAll();

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
            params.add(new ReferenceValue(new RdbmsId(5015, 1)));
            executeQuery(query, 1, params);
            
            query = "select p.login from person p ";
            query += "inner join employee e on (p.id = e.id) ";
            query += "where e.department = {0}";
            params = new ArrayList<Value>();
            params.add(new ReferenceValue(new RdbmsId(5015, 1)));
            executeQuery(query, 1, params);
            
            query = "select e.id from Tst_Employee e ";
            query += "where e.DateOn = {0}";
            params = new ArrayList<Value>();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            DateTimeWithTimeZone dateTimeWithTimeZone = new DateTimeWithTimeZone(TimeZone.getDefault().getID(),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND), 0
                    );
            DateTimeWithTimeZoneValue value = new DateTimeWithTimeZoneValue(dateTimeWithTimeZone);
            params.add(value);
            executeQuery(query, 1, params);

            SortOrder sortOrder = new SortOrder();
            sortOrder.add(new SortCriterion("name", SortCriterion.Order.ASCENDING));
            List<Filter> filters = new ArrayList<Filter>();
            /*Filter filter = new Filter();
            filter.setFilter("byLastName");
            filter.addCriterion(0, null);
            filters.add(filter);*/      
            executeCollection("Employees", 3, sortOrder, filters);
            
            query = "select dateon, DateOff, DateAll from tst_employee";
            executeQuery(query, 3);

            query = "select dateon as works from tst_employee";
            executeQuery(query, 1);

            query = "select dateon as \"works\" from tst_employee";
            executeQuery(query, 1);

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

    private void executeCollection(String collectionName, int columnCount, SortOrder sort, List<Filter> filters) throws Exception {
        IdentifiableObjectCollection collection =
                collectionService.findCollection(collectionName, sort, filters);

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
