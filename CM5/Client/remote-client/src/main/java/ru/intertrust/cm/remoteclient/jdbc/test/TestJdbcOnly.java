package ru.intertrust.cm.remoteclient.jdbc.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ru.intertrust.cm.core.jdbc.JdbcDriver;

public class TestJdbcOnly {
	public static void main(String[] args) {
		try {
			TestJdbcOnly test = new TestJdbcOnly();
			test.execute(args);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void execute(String[] args) throws Exception {
		Class.forName(JdbcDriver.class.getName());

		Connection connection = DriverManager.getConnection(
				"jdbc:sochi:remoting://localhost:4447", "admin", "admin");

		String query = "select t.id, t.login, t.created_date, t.status ";
		query += "from person t ";

		PreparedStatement prepareStatement = connection.prepareStatement(query);

		ResultSet resultset = prepareStatement.executeQuery();

		printResultSet(resultset);

		resultset.close();
		prepareStatement.close();
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
