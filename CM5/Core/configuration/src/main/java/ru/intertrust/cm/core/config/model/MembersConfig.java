package ru.intertrust.cm.core.config.model;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Конфигурация состава динамической группы, внутри может быть один или несколько различных тегов.
 * @author atsvetkov
 */
@Root(name = "members")
public class MembersConfig implements Serializable {

    @Element(name ="collector", required = false)
    private CollectorConfig collector;
    
    @Element(name ="track-domain-objects", required = false)
    private DynamicGroupTrackDomainObjectsConfig trackDomainObjects;

    public CollectorConfig getCollector() {
        return collector;
    }

    public void setCollector(CollectorConfig collector) {
        this.collector = collector;
    }

    public DynamicGroupTrackDomainObjectsConfig getTrackDomainObjects() {
        return trackDomainObjects;
    }

    public void setTrackDomainObjects(DynamicGroupTrackDomainObjectsConfig trackDomainObjects) {
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
