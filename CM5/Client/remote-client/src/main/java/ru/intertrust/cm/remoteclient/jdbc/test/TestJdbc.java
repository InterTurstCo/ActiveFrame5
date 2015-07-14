package ru.intertrust.cm.remoteclient.jdbc.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Calendar;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.jdbc.JdbcDriver;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestJdbc extends ClientBase {

    private CrudService.Remote crudService;
    private CollectionsService.Remote collectionService;

    public static void main(String[] args) {
        try {
            TestJdbc test = new TestJdbc();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        super.execute(args);

        crudService = (CrudService.Remote) getService(
                "CrudServiceImpl", CrudService.Remote.class);

        collectionService = (CollectionsService.Remote) getService(
                "CollectionsServiceImpl", CollectionsService.Remote.class);

        //Создаем тестовый доменный объект
        DomainObject outgoingDocument = createOutgoingDocument();

        //Выполняем запрос с помощью JDBC
        Class.forName(JdbcDriver.class.getName());

        Connection connection = DriverManager.getConnection("jdbc:sochi:remoting://localhost:4447/cm-sochi/web-app", "admin", "admin");

        String query = "select t.id, t.name, t.created_date, t.author, t.long_field, t.status ";
        query += "from Outgoing_Document t ";
        query += "where t.created_date between ? and ? and t.Name = ? and t.Author = ? and t.Author = ? and t.Long_Field = ?";

        PreparedStatement prepareStatement =
                connection.prepareStatement(query);

        Calendar fromDate = Calendar.getInstance();
        fromDate.set(2000, 0, 1);
        Calendar toDate = Calendar.getInstance();
        toDate.set(toDate.get(Calendar.YEAR) + 1, 0, 1);
        prepareStatement.setTimestamp(1, new java.sql.Timestamp(fromDate.getTime().getTime()));
        prepareStatement.setTimestamp(2, new java.sql.Timestamp(toDate.getTime().getTime()));
        prepareStatement.setString(3, "Outgoing_Document");
        prepareStatement.setString(4, outgoingDocument.getReference("Author").toStringRepresentation());
        prepareStatement.setObject(5, outgoingDocument.getReference("Author"));
        prepareStatement.setLong(6, 10);
        ResultSet resultset = prepareStatement.executeQuery();

        printResultSet(query, resultset);
        
        resultset.close();
        prepareStatement.close();
        
        query = "select t.id, t.name, t.created_date, t.author, t.long_field, t.status ";
        query += "from Outgoing_Document t ";
        query += "where (? is null or t.description = ?)";

        prepareStatement =
                connection.prepareStatement(query);

        prepareStatement.setNull(1, Types.VARCHAR);
        prepareStatement.setNull(2, Types.VARCHAR);
        resultset = prepareStatement.executeQuery();

        printResultSet(query, resultset);
        
        resultset.close();
        prepareStatement.close();
        

        query = "select t.name, t.created_date, t.author, t.long_field, t.status, t.id ";
        query += "from Outgoing_Document t ";
        Statement statement = connection.createStatement();
        if (statement.execute(query)){
            resultset = statement.getResultSet();

            printResultSet(query, resultset);
            resultset.close();
            statement.close();

        }

        statement = connection.createStatement();
        if (statement.execute(query)){
            resultset = statement.getResultSet();

            printResultSet(query, resultset);
            
            resultset.close();
            statement.close();
        }
        
        ResultSet tablesRS = connection.getMetaData().getTableTypes();
        printResultSet(query, tablesRS);
        tablesRS.close();

        connection.close();

    }

    private DomainObject createOutgoingDocument() {
        DomainObject document = crudService.createDomainObject("Outgoing_Document");
        document.setString("Name", "Outgoing_Document");
        document.setReference("Author", findDomainObject("person", "login", "person1"));
        document.setLong("Long_Field", 10L);

        document = crudService.save(document);
        return document;
    }

    private DomainObject findDomainObject(String type, String field, String fieldValue) {
        String query = "select t.id from " + type + " t where t." + field + "='" + fieldValue + "'";

        IdentifiableObjectCollection collection = collectionService.findCollectionByQuery(query);
        DomainObject result = null;
        if (collection.size() > 0) {
            result = crudService.find(collection.get(0).getId());
            log("Найден объект " + result.getTypeName() + " " + result.getId());
        }
        return result;
    }

    private void printResultSet(String query, ResultSet resultset) throws SQLException{
        System.out.print(query + "\n");
        System.out.print("№\t");
        for (int i=1; i<=resultset.getMetaData().getColumnCount(); i++ ){
            System.out.print(resultset.getMetaData().getColumnName(i) + "\t");
        }
        System.out.print("\n");
        int rowCount = 0;
        while (resultset.next()) {
            System.out.print(rowCount + "\t");
            for (int i = 1; i <= resultset.getMetaData().getColumnCount(); i++) {
                System.out.print(resultset.getObject(i) + "\t");
            }
            System.out.print("\n");
            rowCount++;
        }
        System.out.println("-----------------------------------------------------------------------");
    }
}
