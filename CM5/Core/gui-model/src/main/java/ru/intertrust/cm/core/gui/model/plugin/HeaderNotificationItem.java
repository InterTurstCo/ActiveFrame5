package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Created by lvov on 01.04.14.
 */
public class HeaderNotificationItem implements Dto {

    private Id id;
    private Id contextObjectId;
    private String subject;
    private String body;

    public HeaderNotificationItem() {

    }

    public HeaderNotificationItem(Id notificationId, Id contextObjectId, String subject, String body) {
        this.id = notificationId;
        this.contextObjectId = contextObjectId;
        this.subject = subject;
        this.body = body;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Id getContextObjectId() {
        return contextObjectId;
    }

    public void setContextObjectId(Id contextObjectId) {
        this.contextObjectId = contextObjectId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HeaderNotificationItem that = (HeaderNotificationItem) o;

        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (contextObjectId != null ? !contextObjectId.equals(that.contextObjectId) : that.contextObjectId != null) return false;
        if (subject != null ? !subject.equals(that.subject) : that.subject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
