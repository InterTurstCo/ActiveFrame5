package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementUnion;

public class NotificationContextObject {

    @Attribute(required = true)
    private String name;

    @ElementUnion({
            @Element(name="java-class", type=FindNotificationContextObjectsJavaClassConfig.class),
            @Element(name="spring-bean", type=FindNotificationContextObjectsSpringBeanConfig.class),
            @Element(name="java-script", type=FindNotificationContextObjectsJavaScriptConfig.class),
            @Element(name="doel", type=FindNotificationContextObjectsDoelConfig.class),
            @Element(name="query", type=FindNotificationContextObjectsQueryConfig.class)
    })
    private FindNotificationContextObjectsConfig findNotificationContextObjectsConfig;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FindNotificationContextObjectsConfig getFindNotificationContextObjectsConfig() {
        return findNotificationContextObjectsConfig;
    }

    public void setFindNotificationContextObjectsConfig(FindNotificationContextObjectsConfig findNotificationContextObjectsConfig) {
        this.findNotificationContextObjectsConfig = findNotificationContextObjectsConfig;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationContextObject that = (NotificationContextObject) o;

        if (findNotificationContextObjectsConfig != null ? !findNotificationContextObjectsConfig.equals(that.findNotificationContextObjectsConfig) : that.findNotificationContextObjectsConfig != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (findNotificationContextObjectsConfig != null ? findNotificationContextObjectsConfig.hashCode() : 0);
        return result;
    }
}
