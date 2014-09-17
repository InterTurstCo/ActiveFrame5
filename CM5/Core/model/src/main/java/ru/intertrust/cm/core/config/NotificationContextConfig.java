package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

public class NotificationContextConfig implements Dto {

    private static final long serialVersionUID = 7775473836468919797L;
    @ElementList(entry="context-object", type=NotificationContextObject.class, inline=true, required = false)
    private List<NotificationContextObject> contextObjects = new ArrayList<NotificationContextObject>();

    public List<NotificationContextObject> getContextObjects() {
        return contextObjects;
    }

    public void setContextObjects(List<NotificationContextObject> contextObjects) {
        this.contextObjects = contextObjects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationContextConfig that = (NotificationContextConfig) o;

        if (contextObjects != null ? !contextObjects.equals(that.contextObjects) : that.contextObjects != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return contextObjects != null ? contextObjects.hashCode() : 0;
    }
}
