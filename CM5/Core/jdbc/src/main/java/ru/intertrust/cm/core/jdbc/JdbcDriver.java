package ru.intertrust.cm.core.jdbc;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Класс JDBC драйвера. jdbc:sochi:remoting://localhost:4447 jdbc:sochi:local
 * @author larin
 * 
 */
public class JdbcDriver implements Driver {

    private static final String DRIVER_PREFIX = "jdbc:sochi";
    private static final String DRIVER_PREFIX_LOCAL = DRIVER_PREFIX + ":local";
    private static final String DRIVER_PREFIX_REMOTING = DRIVER_PREFIX + ":remoting";
    private static final String LOGIN_PROPERY = "user";
    private static final String PASSWORD_PROPERY = "password";

    public enum ConnectMode {
        Local,
        Remoting
    }

    static {
        try {
            DriverManager.registerDriver(new JdbcDriver());
        } catch (SQLException exception) {
            //exception.printStackTrace();
            throw new RuntimeException("Error init driver", exception);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        ConnectMode mode = ConnectMode.Local;
        String login = null;
        String password = null;
        String address = null;
        if (url.startsWith(DRIVER_PREFIX_REMOTING)) {
            mode = ConnectMode.Remoting;
            login = info.getProperty(LOGIN_PROPERY);
            password = info.getProperty(PASSWORD_PROPERY);
            address = url.split("//")[1];
        }
        
        return new JdbcConnection(mode, address, login, password);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith(DRIVER_PREFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMajorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMinorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean jdbcCompliant() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException();
    }
}
