package ru.intertrust.cm.core.jdbc;

import java.net.URI;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Класс JDBC драйвера. 
 * формат строки подключения: 
 * <lo> Для remote интерфейса - jdbc:sochi:remoting://localhost:4447/app-name/module-name 
 * <lo> Для локального интерфейса - jdbc:sochi:local
 * @author larin
 * 
 */
public class JdbcDriver implements Driver {

    private static final String DRIVER_PREFIX = "jdbc-sochi";
    private static final String DRIVER_PREFIX_LOCAL = DRIVER_PREFIX + "-local";
    private static final String DRIVER_PREFIX_REMOTING = DRIVER_PREFIX + "-remoting";
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
            throw new RuntimeException("Error init driver", exception);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        try {
            ConnectMode mode = ConnectMode.Local;
            String login = null;
            String password = null;
            String host = null;
            String port = null;
            String appName = null;
            String moduleName = null;

            
            String uriString = url.replaceFirst(":", "-");
            uriString = uriString.replaceFirst(":", "-");
            URI urlObject = new URI(uriString);

            if (DRIVER_PREFIX_REMOTING.equals(urlObject.getScheme())) {
                mode = ConnectMode.Remoting;
                login = info.getProperty(LOGIN_PROPERY);
                password = info.getProperty(PASSWORD_PROPERY);
                host = urlObject.getHost();
                port = String.valueOf(urlObject.getPort());
                String[] path = urlObject.getPath().split("/");
                if (path.length != 3){
                    new SQLException("Connection string need contains application name and module name. Example:  ");
                }
                appName = path[1];
                moduleName = path[2];
            }else if(uriString.startsWith(DRIVER_PREFIX_LOCAL)){
                mode = ConnectMode.Local;
            }else{
                new SQLException("URI schema not valid. Valid schema: jdbc:sochi:local or jdbc:sochi:remoting");
            }

            return new JdbcConnection(new SochiClient(mode, host, port, login, password, appName, moduleName));
        } catch (SQLException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SQLException("Error make connection", ex);
        }
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
