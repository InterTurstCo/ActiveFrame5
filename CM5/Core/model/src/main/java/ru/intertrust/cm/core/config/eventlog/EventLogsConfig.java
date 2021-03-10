package ru.intertrust.cm.core.config.eventlog;


import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;

public class EventLogsConfig implements Dto {
    @Element(name = "login", required = false)
    private SimpleConfig loginConfig;

    @Element(name = "logout", required = false)
    private SimpleConfig logoutConfig;

    @Element(name = "download-attachment", required = false)
    private SimpleConfig downloadAttachment;

    @Element(name = "domain-object-access", required = false)
    private DomainObjectAccessConfig domainObjectAccess;

    public SimpleConfig getLoginConfig() {
        return loginConfig;
    }

    public void setLoginConfig(SimpleConfig loginConfig) {
        this.loginConfig = loginConfig;
    }

    public SimpleConfig getLogoutConfig() {
        return logoutConfig;
    }

    public void setLogoutConfig(SimpleConfig logoutConfig) {
        this.logoutConfig = logoutConfig;
    }

    public SimpleConfig getDownloadAttachment() {
        return downloadAttachment;
    }

    public void setDownloadAttachment(SimpleConfig downloadAttachment) {
        this.downloadAttachment = downloadAttachment;
    }

    public DomainObjectAccessConfig getDomainObjectAccess() {
        return domainObjectAccess;
    }

    public void setDomainObjectAccess(DomainObjectAccessConfig domainObjectAccess) {
        this.domainObjectAccess = domainObjectAccess;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventLogsConfig that = (EventLogsConfig) o;

        if (loginConfig != null ? !loginConfig.equals(that.loginConfig) : that.loginConfig != null) return false;
        if (logoutConfig != null ? !logoutConfig.equals(that.logoutConfig) : that.logoutConfig != null) return false;
        if (downloadAttachment != null ? !downloadAttachment.equals(that.downloadAttachment) : that.downloadAttachment != null) return false;
        if (domainObjectAccess != null ? !domainObjectAccess.equals(that.domainObjectAccess) : that.domainObjectAccess != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = loginConfig != null ? loginConfig.hashCode() : 0;
        result = 31 * result + (logoutConfig != null ? logoutConfig.hashCode() : 0);
        result = 31 * result + (downloadAttachment != null ? downloadAttachment.hashCode() : 0);
        result = 31 * result + (domainObjectAccess != null ? domainObjectAccess.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EventLogsConfig{" +
                "loginConfig=" + loginConfig +
                ", logoutConfig=" + logoutConfig +
                ", downloadAttachment=" + downloadAttachment +
                ", domainObjectAccess=" + domainObjectAccess +
                '}';
    }
}
