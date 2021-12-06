package ru.intertrust.cm.core.config.eventlog;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

import java.util.List;

public class LogDomainObjectAccessConfig  implements Dto {

    @Attribute
    private boolean enable;

    @Attribute
    private String accessType;

    @Attribute
    private String accessWasGranted;

    @ElementList(inline = true, entry ="domain-object-type")
    private List<DomainObjectTypeConfig> domainObjectTypeConfigList;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getAccessWasGranted() {
        return accessWasGranted;
    }

    public void setAccessWasGranted(String accessWasGranted) {
        this.accessWasGranted = accessWasGranted;
    }

    public List<DomainObjectTypeConfig> getDomainObjectTypeConfigList() {
        return domainObjectTypeConfigList;
    }

    public void setDomainObjectTypeConfigList(List<DomainObjectTypeConfig> domainObjectTypeConfigList) {
        this.domainObjectTypeConfigList = domainObjectTypeConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogDomainObjectAccessConfig that = (LogDomainObjectAccessConfig) o;

        if (enable != that.enable) return false;
        if (accessType != null ? !accessType.equals(that.accessType) : that.accessType != null) return false;
        if (accessWasGranted != null ? !accessWasGranted.equals(that.accessWasGranted) : that.accessWasGranted != null)
            return false;
        if (domainObjectTypeConfigList != null ? !domainObjectTypeConfigList.equals(that.domainObjectTypeConfigList) : that.domainObjectTypeConfigList != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (enable ? 1 : 0);
        result = 31 * result + (accessType != null ? accessType.hashCode() : 0);
        result = 31 * result + (accessWasGranted != null ? accessWasGranted.hashCode() : 0);
        result = 31 * result + (domainObjectTypeConfigList != null ? domainObjectTypeConfigList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LogDomainObjectAccessConfig{" +
                "enable=" + enable +
                ", accessType='" + accessType + '\'' +
                ", accessWasGranted='" + accessWasGranted + '\'' +
                ", domainObjectTypeConfigList=" + domainObjectTypeConfigList +
                '}';
    }
}
