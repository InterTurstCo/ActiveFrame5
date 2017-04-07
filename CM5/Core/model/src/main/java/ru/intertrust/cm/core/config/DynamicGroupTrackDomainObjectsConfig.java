package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;

/**
 * Класс конфигурации коллектора динамической группы
 * @author larin
 *
 */
public class DynamicGroupTrackDomainObjectsConfig extends TrackDomainObjectsConfig {
    @Element(name ="get-person", required = false)
    private GetPersonConfig getPerson;

    public GetPersonConfig getGetPerson() {
        return getPerson;
    }

    public void setGetPerson(GetPersonConfig getPerson) {
        this.getPerson = getPerson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DynamicGroupTrackDomainObjectsConfig that = (DynamicGroupTrackDomainObjectsConfig) o;

        if (getPerson != null ? !getPerson.equals(that.getPerson) : that.getPerson != null) return false;

        return true;
    }
}
