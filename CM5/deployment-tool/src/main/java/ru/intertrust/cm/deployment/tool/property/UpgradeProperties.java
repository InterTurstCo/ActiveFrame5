package ru.intertrust.cm.deployment.tool.property;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * Created by Alexander Bogatyrenko on 26.07.16.
 * <p>
 * This class represents...
 */
@Component
public class UpgradeProperties {

    @NotEmpty
    @Value("${jboss.home}")
    private String jbossHome;

    @NotEmpty
    @Value("${jboss.host}")
    private String jbossHost;

    @NotEmpty
    @Value("${jboss.app.port}")
    private String jbossAppPort;

    @NotEmpty
    @Value("${jboss.app.url}")
    private String jbossAppUrl;

    @NotEmpty
    @Value("${jboss.admin.port}")
    private String jbossAdminPort;

    @NotEmpty
    @Value("${jboss.user}")
    private String jbossUser;

    @NotEmpty
    @Value("${jboss.password}")
    private String jbossPassword;

    @NotEmpty
    @Value("${jboss.cli.controller}")
    private String jbossCliController;

    @NotEmpty
    @Value("${jboss.cli.port}")
    private String jbossCliPort;

    @NotEmpty
    @Value("${server.properties}")
    private String serverProperties;

    @NotEmpty
    @Value("${postgres.home}")
    private String postgresHome;

    @NotEmpty
    @Value("${db.host}")
    private String dbHost;

    @NotEmpty
    @Value("${db.port}")
    private String dbPort;

    @NotEmpty
    @Value("${db.name}")
    private String dbName;

    @NotEmpty
    @Value("${db.user}")
    private String dbUser;

    @NotEmpty
    @Value("${db.password}")
    private String dbPassword;

    @NotEmpty
    @Value("${ear.folder}")
    private String earFolder;

    @NotEmpty
    @Value("${ear.current}")
    private String earCurrent;

    @NotEmpty
    @Value("${ear.sequence}")
    private List<String> earSequence;

    @Value("${initial.data.folder}")
    private String initialDataFolder;

    @NotEmpty
    @Value("${backup.folder}")
    private String backupFolder;

    @NotEmpty
    @Value("${backup.before}")
    private Set<String> backupBefore;

    @NotNull
    @Value("${restore.version.on.failure}")
    private RestoreVersionType restoreVersionOnFailure;

    public String getJbossHome() {
        return jbossHome;
    }

    public String getJbossHost() {
        return jbossHost;
    }

    public String getJbossAppPort() {
        return jbossAppPort;
    }

    public String getJbossAppUrl() {
        return jbossAppUrl;
    }

    public String getJbossAdminPort() {
        return jbossAdminPort;
    }

    public String getJbossUser() {
        return jbossUser;
    }

    public String getJbossPassword() {
        return jbossPassword;
    }

    public String getJbossCliController() {
        return jbossCliController;
    }

    public String getJbossCliPort() {
        return jbossCliPort;
    }

    public String getServerProperties() {
        return serverProperties;
    }

    public String getPostgresHome() {
        return postgresHome;
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getEarFolder() {
        return earFolder;
    }

    public String getEarCurrent() {
        return earCurrent;
    }

    public List<String> getEarSequence() {
        return earSequence;
    }

    public String getInitialDataFolder() {
        return initialDataFolder;
    }

    public String getBackupFolder() {
        return backupFolder;
    }

    public Set<String> getBackupBefore() {
        return backupBefore;
    }

    public RestoreVersionType getRestoreVersionOnFailure() {
        return restoreVersionOnFailure;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpgradeProperties{");
        sb.append("jbossHome='").append(jbossHome).append('\'');
        sb.append(", jbossHost='").append(jbossHost).append('\'');
        sb.append(", jbossAppPort='").append(jbossAppPort).append('\'');
        sb.append(", jbossAppUrl='").append(jbossAppUrl).append('\'');
        sb.append(", jbossAdminPort='").append(jbossAdminPort).append('\'');
        sb.append(", jbossUser='").append(jbossUser).append('\'');
        sb.append(", jbossPassword='").append(jbossPassword).append('\'');
        sb.append(", jbossCliController='").append(jbossCliController).append('\'');
        sb.append(", jbossCliPort='").append(jbossCliPort).append('\'');
        sb.append(", serverProperties='").append(serverProperties).append('\'');
        sb.append(", postgresHome='").append(postgresHome).append('\'');
        sb.append(", dbHost='").append(dbHost).append('\'');
        sb.append(", dbPort='").append(dbPort).append('\'');
        sb.append(", dbName='").append(dbName).append('\'');
        sb.append(", dbUser='").append(dbUser).append('\'');
        sb.append(", dbPassword='").append(dbPassword).append('\'');
        sb.append(", earFolder='").append(earFolder).append('\'');
        sb.append(", earCurrent='").append(earCurrent).append('\'');
        sb.append(", earSequence=").append(earSequence);
        sb.append(", initialDataFolder='").append(initialDataFolder).append('\'');
        sb.append(", backupFolder='").append(backupFolder).append('\'');
        sb.append(", backupBefore=").append(backupBefore);
        sb.append(", restoreVersionOnFailure=").append(restoreVersionOnFailure);
        sb.append('}');
        return sb.toString();
    }
}
