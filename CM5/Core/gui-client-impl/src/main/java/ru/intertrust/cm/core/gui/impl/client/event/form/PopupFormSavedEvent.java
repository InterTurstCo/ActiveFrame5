package ru.intertrust.cm.core.gui.impl.client.event.form;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Created by 1 on 04.03.2016.
 */
public class PopupFormSavedEvent extends GwtEvent<PopupFormSavedEventHandler> {
    public static Type<PopupFormSavedEventHandler> TYPE = new Type<PopupFormSavedEventHandler>();
    private Id savedObjectId;

    @Override
    public Type<PopupFormSavedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PopupFormSavedEventHandler handler) {
        handler.onPopupFormSavedEvent(this);
    }

    public PopupFormSavedEvent(Id savedObjectId){
        this.savedObjectId = savedObjectId;
    }

    public PopupFormSavedEvent(){}

    public Id getSavedObjectId() {
        return savedObjectId;
    }

    public void setSavedObjectId(Id savedObjectId) {
        this.savedObjectId = savedObjectId;
    }
}
