package ru.intertrust.cm.core.config.eventlog;

import org.simpleframework.xml.ElementList;

import java.util.List;

public class DomainObjectAccessConfig extends SimpleConfig {



    @ElementList(inline = true, entry ="log", required = false)
    private List<LogDomainObjectAccessConfig> logs;

    public List<LogDomainObjectAccessConfig> getLogs() {
        return logs;
    }

    public void setLogs(List<LogDomainObjectAccessConfig> logs) {
        this.logs = logs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DomainObjectAccessConfig that = (DomainObjectAccessConfig) o;

        if (logs != null ? !logs.equals(that.logs) : that.logs != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (logs != null ? logs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DomainObjectAccessConfig{" +
                "logs=" + logs +
                '}';
    }
}
