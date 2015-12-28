package ru.intertrust.cm.core.gui.impl.client.event;


import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 25.12.2015
 */
public class FormSavedEvent extends GwtEvent<FormSavedEventHandler> {

    private Boolean readOnly;
    private int viewHashcode;
    private FormState formState;
    public static Type<FormSavedEventHandler> TYPE = new GwtEvent.Type<>();

    public FormSavedEvent(Boolean readOnly, int viewHashcode, FormState formState) {
        this.readOnly = readOnly;
        this.viewHashcode = viewHashcode;
        this.formState = formState;
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

    public FormState getFormState() {
        return formState;
    }

    public void setFormState(FormState formState) {
        this.formState = formState;
    }
}
