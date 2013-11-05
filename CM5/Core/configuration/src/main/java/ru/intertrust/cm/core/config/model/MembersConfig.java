package ru.intertrust.cm.core.config.model;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Конфигурация состава динамической группы, внутри может быть один или несколько различных тегов.
 * @author atsvetkov
 */
@Root(name = "members")
public class MembersConfig implements Serializable {

    @ElementList(required = false, inline=true)
    private List<CollectorConfig> collector;
    
    @ElementList(required = false, inline=true)
    private List<DynamicGroupTrackDomainObjectsConfig> trackDomainObjects;

    public List<CollectorConfig> getCollector() {
        return collector;
    }

    public void setCollector(List<CollectorConfig> collector) {
        this.collector = collector;
    }

    public List<DynamicGroupTrackDomainObjectsConfig> getTrackDomainObjects() {
        return trackDomainObjects;
    }

    public void setTrackDomainObjects(List<DynamicGroupTrackDomainObjectsConfig> trackDomainObjects) {
        this.trackDomainObjects = trackDomainObjects;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MembersConfig that = (MembersConfig) o;

        if (collector != null ? !collector.equals(that.collector) : that.collector != null) {
            return false;
        }
        if (trackDomainObjects != null ? !trackDomainObjects.equals(that.trackDomainObjects) : that.trackDomainObjects != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = trackDomainObjects != null ? trackDomainObjects.hashCode() : 0;
        result = 31 * result + (collector != null ? collector.hashCode() : 0);
        return result;
    }
}
