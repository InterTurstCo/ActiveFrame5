package ru.intertrust.cm.core.service.it;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.RdbmsId;

public class JdbcRemoteIT extends IntegrationTestBase{
    @EJB
    private CollectionsService.Remote collectionService;

    @EJB
    private CrudService.Remote crudService;
    
    @Before
    public void init() throws Exception {
        crudService = (CrudService.Remote) getRemoteService(
                "CrudServiceImpl", CrudService.Remote.class, "admin", "admin");

        collectionService = (CollectionsService.Remote) getRemoteService(
                "CollectionsServiceImpl", CollectionsService.Remote.class, "admin", "admin");
    }

    @Test
    public void testPreparedStatement() throws Exception {
        Connection connection = null;
        ResultSet resultset = null;
        PreparedStatement prepareStatement = null;
        try {

            //Создаем тестовый доменный объект
            DomainObject outgoingDocument = greateOutgoingDocument();

            //Выполняем запрос с помощью JDBC
            Class.forName("ru.intertrust.cm.core.jdbc.JdbcDriver");

            connection = DriverManager.getConnection("jdbc:sochi:remoting://localhost:4447", "admin", "admin");
            //connection = DriverManager.getConnection("jdbc:sochi:local");

            String query = "select t.id, t.name, t.created_date, t.author, t.long_field, t.status ";
            query += "from Outgoing_Document t ";
            query += "where t.created_date between ? and ? and t.Name = ? and t.Author = ? and t.Long_Field = ?";

            prepareStatement =
                    connection.prepareStatement(query);

            Calendar fromDate = Calendar.getInstance();
            fromDate.set(2000, 0, 1);
            prepareStatement.setTimestamp(1, new java.sql.Timestamp(fromDate.getTime().getTime()));
            prepareStatement.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            prepareStatement.setString(3, "Outgoing_Document");
            prepareStatement.setLong(4, ((RdbmsId) outgoingDocument.getReference("Author")).getId());
            prepareStatement.setLong(5, 10);
            resultset = prepareStatement.executeQuery();

            printResultSet(resultset);
            Assert.assertTrue(resultset.getMetaData().getColumnCount() == 6);

            resultset.close();
            prepareStatement.close();

        } finally {
            try {
                resultset.close();
                prepareStatement.close();
                connection.close();
            } catch (Exception ignoreEx) {
            }
        }
    }

    @Test
    public void testStatement() throws Exception {
        Connection connection = null;
        ResultSet resultset = null;
        Statement statement = null;
        try {

            //Создаем тестовый доменный объект
            greateOutgoingDocument();

            //Грузим драйвер
            Class.forName("ru.intertrust.cm.core.jdbc.JdbcDriver");

            //Создаем соединение
            connection = DriverManager.getConnection("jdbc:sochi:remoting://localhost:4447", "admin", "admin");
            //connection = DriverManager.getConnection("jdbc:sochi:local");

            //Выполняем запрос с помощью JDBC
            String query = "select t.name, t.created_date, t.author, t.long_field, t.status, t.id ";
            query += "from Outgoing_Document t ";
            statement = connection.createStatement();
            if (statement.execute(query)) {
                resultset = statement.getResultSet();
                printResultSet(resultset);
                Assert.assertTrue(resultset.getMetaData().getColumnCount() == 6);
                Assert.assertTrue(resultset.getMetaData().getColumnName(6).equals("id"));
            }

        } finally {
            try {
                resultset.close();
                statement.close();
                connection.close();
            } catch (Exception ignoreEx) {
            }
        }
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
        }
        return result;
    }

    private void printResultSet(ResultSet resultset) throws SQLException {
        System.out.print("№\t");
        for (int i = 1; i <= resultset.getMetaData().getColumnCount(); i++) {
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
        Assert.assertTrue(rowCount > 0);
    }

}
