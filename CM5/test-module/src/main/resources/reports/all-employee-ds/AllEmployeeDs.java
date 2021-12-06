//package reports.all;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import ru.intertrust.cm.core.service.api.ReportDS;

public class AllEmployeeDs implements ReportDS {

    public JRDataSource getJRDataSource(Connection connection, Map<String, Object> params) throws Exception {
        List<Person> result = new ArrayList<>();

        PreparedStatement statement = connection.prepareStatement("select t.login from person t");
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            Person row = new Person();
            row.setLogin(resultSet.getString("login"));
            result.add(row);
        }
        statement.close();

        return new JRBeanCollectionDataSource(result);
    }

    public class Person {
        private String login;
        private int hash;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public int getHash() {
            return this.login.hashCode();
        }

        public void setHash(int hash) {
            this.hash = hash;
        }
    }
}
