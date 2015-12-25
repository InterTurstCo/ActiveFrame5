package ru.intertrust.cm.core.gui.impl.client.event;


import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 25.12.2015
 */
public class FormSavedEvent extends GwtEvent<FormSavedEventHandler> {

    private Boolean readOnly;
    private int viewHashcode;
    public static Type<FormSavedEventHandler> TYPE = new GwtEvent.Type<>();

    public FormSavedEvent(Boolean readOnly, int viewHashcode) {
        this.readOnly = readOnly;
        this.viewHashcode = viewHashcode;
    }

    @Override
    public Type<FormSavedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FormSavedEventHandler handler) {
        handler.afterFormSaved(this);
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public int getViewHashcode() {
        return viewHashcode;
    }

    public void setViewHashcode(int viewHashcode) {
        this.viewHashcode = viewHashcode;
    }
}
