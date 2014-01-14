package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

@Root(name = "global-settings")
public class GlobalSettingsConfig implements TopLevelConfig {
    /**
     *
     */
    private static final long serialVersionUID = -8166587368979922484L;
    public static final String NAME = "global-settings";

    @Element(name = "audit-log", required = true)
    private AuditLog auditLog;

    @Element(name = "sql-trace", required = true)
    private SqlTrace sqlTrace;


    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public SqlTrace getSqlTrace() {
        return sqlTrace;
    }

    public void setSqlTrace(SqlTrace sqlTrace) {
        this.sqlTrace = sqlTrace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GlobalSettingsConfig that = (GlobalSettingsConfig) o;

        if (auditLog != null ? !auditLog.equals(that.auditLog) : that.auditLog != null) {
            return false;
        }
        if (sqlTrace != null ? !sqlTrace.equals(that.sqlTrace) : that.sqlTrace != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = auditLog != null ? auditLog.hashCode() : 0;
        result = 31 * result + (sqlTrace != null ? sqlTrace.hashCode() : 0);
        return result;
    }

    @Override
    public String getName() {
        return "";
    }
}
