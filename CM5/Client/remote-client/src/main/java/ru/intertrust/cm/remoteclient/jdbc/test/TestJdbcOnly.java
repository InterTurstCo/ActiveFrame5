package ru.intertrust.cm.remoteclient.jdbc.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ru.intertrust.cm.core.jdbc.JdbcDriver;
import ru.intertrust.cm.remoteclient.ClientBase;

public class TestJdbcOnly extends ClientBase {
    public static void main(String[] args) {
        try {
            TestJdbcOnly test = new TestJdbcOnly();
            test.execute(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(String[] args) throws Exception {
        Class.forName(JdbcDriver.class.getName());
        String psswd = getAppPropery("jdbc.password");;
        Connection connection = DriverManager.getConnection(
                "jdbc:sochi:remoting://localhost:4447/cm-sochi/web-app", "admin", psswd);

        String query = "select t.id, t.login, t.created_date, t.status ";
        query += "from person t ";
        executeQuery(connection, query);

        executeQuery(connection, "select dateon, DateOff, DateAll from tst_employee");

        executeQuery(connection, "select id from tst_employee where (active=? and 1<>?)", true, true);

        connection.close();
    }

    private void executeQuery(Connection connection, String sql, Object... params) throws Exception {
        try(PreparedStatement prepareStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Boolean) {
                    prepareStatement.setBoolean(i + 1, (Boolean) param);
                }
            }

            try(ResultSet resultset = prepareStatement.executeQuery()) {

                printResultSet(resultset);

            }
        }
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
    }
}
