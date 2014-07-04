package ru.intertrust.cm.core.config;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.search.SearchLanguageConfig;

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

    @Element(name = "transaction-trace", required = true)
    private TransactionTrace transactionTrace;

    @ElementList(name = "search-languages", entry = "language", required = false)
    private List<SearchLanguageConfig> searchLanguages;

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

    public TransactionTrace getTransactionTrace() {
        return transactionTrace;
    }

    public void setTransactionTrace(TransactionTrace transactionTrace) {
        this.transactionTrace = transactionTrace;
    }

    public List<SearchLanguageConfig> getSearchLanguages() {
        return searchLanguages;
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
        if (transactionTrace != null ? !transactionTrace.equals(that.transactionTrace) : that.transactionTrace != null) {
            return false;
        }
        if (searchLanguages != null ? !searchLanguages.equals(that.searchLanguages) : that.searchLanguages != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = auditLog != null ? auditLog.hashCode() : 0;
        result = 31 * result + (sqlTrace != null ? sqlTrace.hashCode() : 0);
        result = 31 * result + (transactionTrace != null ? transactionTrace.hashCode() : 0);
        result = 31 * result + (searchLanguages != null ? searchLanguages.hashCode() : 0);
        return result;
    }

    @Override
    public String getName() {
        return "";
    }
}
