package ru.intertrust.cm.remoteclient.jdbc.test;

import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;
import ru.intertrust.cm.core.jdbc.JdbcDriver;
import ru.intertrust.cm.remoteclient.ClientBase;

import java.sql.*;
import java.util.Calendar;
import java.util.Collection;

public class TestJdbcMultythread extends ClientBase {

    private CrudService.Remote crudService;
    private CollectionsService.Remote collectionService;
    private ConfigurationService configService;

    public static void main(String[] args) {
        try {
            TestJdbcMultythread test = new TestJdbcMultythread();
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

        configService = (ConfigurationService) getService(
                "ConfigurationServiceImpl", ConfigurationService.Remote.class);

        Collection<DomainObjectTypeConfig> configs = configService.getConfigs(DomainObjectTypeConfig.class);

        //Создаем тестовый доменный объект
        final DomainObject outgoingDocument = greateOutgoingDocument();

        Thread t1 = new Thread(new TestJdbcRunable(outgoingDocument));
        t1.start();
        Thread t2 = new Thread(new TestJdbcRunable(outgoingDocument));
        t2.start();

        t1.join();
        t2.join();

    }

    private class TestJdbcRunable implements Runnable {
        private DomainObject outgoingDocument;

        private TestJdbcRunable(DomainObject outgoingDocument) {
            this.outgoingDocument = outgoingDocument;
        }

        @Override
        public void run() {
            Connection connection = null;
            try {
                Class.forName(JdbcDriver.class.getName());

                String pswd = getAppPropery("jdbc.password");;
                connection =
                        DriverManager.getConnection("jdbc:sochi:remoting://localhost:4447/cm-sochi/web-app", "admin", pswd);

                String query = "select t.name, t.created_date, t.author, t.long_field ";
                query += "from Outgoing_Document t ";
                String queryWhere = "where created_date between ? and ? and Name = ? and Author = ? and Long_Field = ?";

                try(PreparedStatement prepareStatement = connection.prepareStatement(query + queryWhere)) {

                    Calendar fromDate = Calendar.getInstance();
                    fromDate.set(2000, 1, 1);
                    prepareStatement.setDate(1, new java.sql.Date(fromDate.getTime().getTime()));
                    prepareStatement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                    prepareStatement.setString(3, "Outgoing_Document");
                    prepareStatement.setObject(4, outgoingDocument.getReference("Author"));
                    prepareStatement.setLong(5, 10);
                    try(ResultSet resultset = prepareStatement.executeQuery()) {

                        int rowCount = 0;
                        while (resultset.next()) {
                            System.out.println(rowCount + "\t" + resultset.getString(1) + "\t" + resultset.getDate(2)
                                    + "\t" + resultset.getObject(3) + "\t" + resultset.getObject(4));
                            rowCount++;
                        }
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    if (statement.execute(query)) {
                        try(ResultSet resultset = statement.getResultSet()) {

                            int rowCount = 0;
                            while (resultset.next()) {
                                System.out.println(rowCount + "\t" + resultset.getString(1) + "\t" + resultset.getDate(2)
                                        + "\t" + resultset.getObject(3) + "\t" + resultset.getObject(4));
                                rowCount++;
                            }
                        }
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    if (statement.execute(query)) {
                        try(ResultSet resultset = statement.getResultSet()) {

                            int rowCount = 0;
                            while (resultset.next()) {
                                System.out.println(rowCount + "\t" + resultset.getString(1) + "\t" + resultset.getDate(2)
                                        + "\t" + resultset.getObject(3) + "\t" + resultset.getObject(4));
                                rowCount++;
                            }
                        }


                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception ignoreEx) {
                    }
                }
            }
        }

    }

    private DomainObject greateOutgoingDocument() {
        DomainObject document = crudService.createDomainObject("Outgoing_Document");
        document.setString("Name", "Outgoing_Document");
        document.setReference("Author", findDomainObject("Employee", "Name", "Сотрудник 1"));
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
