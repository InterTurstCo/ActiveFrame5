package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 28.04.2015
 */
public class ShowAttachmentEvent extends GwtEvent<ShowAttachmentEventHandler> {
    public static final Type<ShowAttachmentEventHandler> TYPE = new Type<ShowAttachmentEventHandler>();
    Id objectId;
    String viewerId;

    public Id getObjectId() {
        return objectId;
    }

    public void setObjectId(Id objectId) {
        this.objectId = objectId;
    }

    public String getViewerId() {
        return viewerId;
    }

    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    public ShowAttachmentEvent(Id objectId, String viewerId){
        this.objectId = objectId;
        this.viewerId = viewerId;
    }

    @Override
    public Type<ShowAttachmentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ShowAttachmentEventHandler showAttachmentEventHandler) {
        showAttachmentEventHandler.showAttachmentEvent(this);
    }
}
