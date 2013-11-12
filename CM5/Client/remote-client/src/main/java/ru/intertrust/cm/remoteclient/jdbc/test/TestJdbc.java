package ru.intertrust.cm.remoteclient.jdbc.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;
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
        DomainObject outgoingDocument = greateOutgoingDocument();

        //Выполняем запрос с помощью JDBC
        Class.forName(JdbcDriver.class.getName());

        Connection connection = DriverManager.getConnection("jdbc:sochi:remoting://localhost:4447", "admin", "admin");

        String query = "select t.name, t.created_date, t.author, t.long_field ";
        query += "from Outgoing_Document t ";
        //query += "where creation_date between ? and ? and Name = ? and Author = ? and Long_Field = ?";

        PreparedStatement prepareStatement =
                connection.prepareStatement(query);

        Calendar fromDate = Calendar.getInstance();
        fromDate.set(2000, 1, 1);
        prepareStatement.setDate(0, new java.sql.Date(fromDate.getTime().getTime()));
        prepareStatement.setDate(1, new java.sql.Date(System.currentTimeMillis()));
        prepareStatement.setString(2, "Outgoing_Document");
        prepareStatement.setLong(3, ((RdbmsId) outgoingDocument.getReference("Author")).getId());
        prepareStatement.setLong(4, 10);
        ResultSet resultset = prepareStatement.executeQuery();

        int rowCount = 0;
        while (resultset.next()) {
            System.out.println(rowCount + "\t" + resultset.getString(1) + "\t" + resultset.getDate(2) + "\t" + resultset.getLong(3) + "\t" + resultset.getLong(4));
            rowCount++;
        }
        resultset.close();
        prepareStatement.close();
        
        Statement statement = connection.createStatement();
        if (statement.execute(query)){
            resultset = statement.getResultSet();

            rowCount = 0;
            while (resultset.next()) {
                System.out.println(rowCount + "\t" + resultset.getString(1) + "\t" + resultset.getDate(2) + "\t" + resultset.getLong(3) + "\t" + resultset.getLong(4));
                rowCount++;
            }
            resultset.close();
            statement.close();
        
        }
        
        connection.close();
        
    }

    private DomainObject greateOutgoingDocument() {
        DomainObject document = crudService.createDomainObject("Outgoing_Document");
        document.setString("Name", "Outgoing_Document");
        document.setReference("Author", findDomainObject("Employee", "Name", "Employee-1"));
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

}
