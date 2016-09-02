package ru.intertrust.cm.deployment.tool.service;

import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.intertrust.cm.deployment.tool.property.UpgradeProperties;
import ru.intertrust.cm.deployment.tool.util.ProcessPrintUtil;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Alexander Bogatyrenko on 28.07.16.
 * <p>
 * This class represents...
 */
@Service
public class PostgresService {

    private static Logger logger = LoggerFactory.getLogger(PostgresService.class);

    @Autowired
    private UpgradeProperties props;

    @Autowired
    private Boolean isWindowsEnv;

    public void create(String earVersion) {
        final List<String> commands = new ArrayList<>(12);
        commands.add(Paths.get(props.getPostgresHome(), "bin", isWindowsEnv ? "pg_dump.exe" : "pg_dump").toString());
        commands.add("-c");
        commands.add("-v");
        commands.add("-f");
        commands.add(Paths.get(props.getBackupFolder(), earVersion, earVersion + "-db.backup").toString());
        commands.add(props.getDbName());
        commands.add("-h");
        commands.add(props.getDbHost());
        commands.add("-p");
        commands.add(props.getDbPort());
        commands.add("-U");
        commands.add(props.getDbUser());

        try {
            logger.info("Backup create start");
            start(commands);
            logger.info("Backup create stop");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void restore(String earVersion) {
        final List<String> commands = new ArrayList<>(11);
        commands.add(Paths.get(props.getPostgresHome(), "bin", isWindowsEnv ? "psql.exe" : "psql").toString());
        commands.add(props.getDbName());
        commands.add("-f");
        commands.add(Paths.get(props.getBackupFolder(), earVersion, earVersion + "-db.backup").toString());
        commands.add("-v");
        commands.add("-h");
        commands.add(props.getDbHost());
        commands.add("-p");
        commands.add(props.getDbPort());
        commands.add("-U");
        commands.add(props.getDbUser());

        try {
            logger.info("Backup restore start");
            start(commands);
            logger.info("Backup restore stop");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void start(List<String> commands) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.environment().put("PGPASSWORD", props.getDbPassword());
        builder.redirectErrorStream(true);
        Process process = builder.start();
        ProcessPrintUtil.print(process);
    }

    public boolean testConnection() {
        logger.info("Start test database connection");
        try {
            DriverManager.registerDriver(new Driver());
            String url = "jdbc:postgresql://" + props.getDbHost() + ":" + props.getDbPort() + "/" + props.getDbName();
            Properties properties = new Properties();
            properties.setProperty("user", props.getDbUser());
            properties.setProperty("password",props.getDbPassword());
            Connection connection = DriverManager.getConnection(url, properties);
            boolean connect = connection != null;
            if (connect) {
                logger.info("Database connection - success");
                connection.close();
            } else {
                logger.error("Database connection - fail");
            }
            return connect;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
}
