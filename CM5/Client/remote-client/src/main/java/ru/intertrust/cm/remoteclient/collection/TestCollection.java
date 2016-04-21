package ru.intertrust.cm.remoteclient.collection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestCollection extends ClientBase {

    protected CollectionsService.Remote collectionService;
    
    protected CrudService.Remote crudService;

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
            
            query = "select id from employee where (select count(e.id) from employee e where e.name = 'xxx') > 0";
            executeQuery(query, 1);
            
            //Расскоментировать после исправления CMFIVE-1220
            params.clear();
            params.add(new ReferenceValue(new RdbmsId(5018, 1)));
            query = "select x.id, x.col2 from ( ";
            query += "select t.id, t.created_date as col2, t.organization as col3 from department t ";
            query += "union ";
            query += "select t.id, t.created_date as col2, t.organization as col3 from department t ";
            query += ") x where x.col3 = {0}";
            executeQuery(query, 2, params);

            // Расскоментировать после исправления CMFIVE-1225 
            params.clear();
            List <Value> listParam = new ArrayList<Value>();
            listParam.add(new ReferenceValue(new RdbmsId(5018, 1)));
            listParam.add(new ReferenceValue(new RdbmsId(5018, 2)));
            listParam.add(new ReferenceValue(new RdbmsId(5018, 3)));
            params.add(new ListValue(listParam));
            query = "select id, name from organization where id in ({0})";
            executeQuery(query, 2, params);
            
            
            query = "select id, dateon, dateoff, dateall from tst_employee";
            executeQuery(query, 4);
            
            query = "select id from ("
                    + "select x.id from ("
                    + "select e.id, e.position, e.department as dpt "
                    + "from tst_employee e) x "
                    + "inner join person p on p.id = x.id "
                    + "where x.dpt = {0}) y";
            params.clear();
            params.add(new ReferenceValue(new RdbmsId(5015, 1)));
            executeQuery(query, 1, params);
            
            //test CMFIVE-2150
            query = "SELECT ";
            query += "d.id ";
            query += "FROM ";
            query += "(SELECT id ";
            query += "FROM employee) e ";
            query += "join department d on d.boss = e.id ";
            query += "WHERE ";
            query += "d.boss={0} ";
            params.clear();
            params.add(new ReferenceValue(new RdbmsId(5064, 1)));
            executeQuery(query, 1, params);

            query = "SELECT ";
            query += "d.name as boss ";
            query += "FROM ";
            query += "employee e ";
            query += "join department d on d.boss = e.id ";
            query += "WHERE ";
            query += "e.id={0} ";
            params.clear();
            params.add(new ReferenceValue(new RdbmsId(5013, 2)));
            executeQuery(query, 1, params);  
            
            //Проверка CMFIVE-2196
            query = "SELECT ";
            query += "d.boss ";
            query += "FROM ";
            query += "(SELECT id ";
            query += "FROM employee) e ";
            query += "join department d on d.boss = e.id ";
            query += "WHERE ";
            query += "d.boss={0} ";
            params.clear();
            params.add(new ReferenceValue(new RdbmsId(5013, 2)));
            executeQuery(query, 1, params);            

            query = "SELECT ";
            query += "e.id as id, ";
            query += "p.Profile, ";
            query += "p.created_date ";
            query += "FROM ";
            query += "employee e ";
            query += "join person p on p.id = e.id and p.id_type = e.id_type ";
            query += "where p.login is not null and profile = {0}";
            params.clear();
            params.add(new ReferenceValue(new RdbmsId(5036, 2)));
            executeQuery(query, 3, params);      
            
            sortOrder = new SortOrder();
            filters = new ArrayList<Filter>();
            Filter filter = new Filter();
            filter.setFilter("PROFILE");
            filter.addCriterion(0, new ReferenceValue(new RdbmsId(5036, 2)));
            filters.add(filter);   
            executeCollection("Employee_Person", 3, sortOrder, filters);            
            
            query = "SELECT orgDescr.Module FROM SO_OrgDescriptionSys so_org_desc_sys " +
                    "join SO_OrgDescription orgDescr on orgDescr.id = so_org_desc_sys.id and orgDescr.id_type = so_org_desc_sys.id_type  " +
                    "WHERE  orgDescr.IsDeleted=0 and orgDescr.Edited is null and module = {0}";
            
            params.clear();
            params.add(new ReferenceValue(new RdbmsId(5110, 1)));
            executeQuery(query, 1, params);            

            query = "select id, login from person where id in ({0})";
            
            params.clear();
            listParam.clear();
            listParam.add(new ReferenceValue(new RdbmsId(5018, 1)));
            listParam.add(new ReferenceValue(new RdbmsId(5018, 2)));
            params.add(new ListValue(listParam));
            executeQuery(query, 2, params);            

            listParam.remove(0);
            params.clear();
            params.add(new ListValue(listParam));
            executeQuery(query, 2, params);         
            
            query = "select id from person where login = {0}";
            
            params.clear();
            params.add(new StringValue(null));
            executeQuery(query, 1, params);
            
            //Тест запроса класса
            sortOrder = new SortOrder();
            sortOrder.add(new SortCriterion("name", SortCriterion.Order.ASCENDING));
            filters = new ArrayList<Filter>();
            executeCollection("EmployeesGenerator", 3, sortOrder, filters);
            
        } finally {
            writeLog();
        }
    }

    protected void executeQuery(String query, int columnCount) throws Exception {
        executeQuery(query, columnCount, null);
    }

    protected void executeQuery(String query, int columnCount, List<Value> params) throws Exception {
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

    protected void executeCollection(String collectionName, int columnCount, SortOrder sort, List<Filter> filters) throws Exception {
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
