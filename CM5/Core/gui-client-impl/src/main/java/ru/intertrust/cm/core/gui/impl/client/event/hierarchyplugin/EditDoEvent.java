package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 06.09.2016
 * Time: 11:51
 * To change this template use File | Settings | File and Code Templates.
 */
public class EditDoEvent extends GwtEvent<EditDoEventHandler> {
    private Id doToEdit;
    public static final Type<EditDoEventHandler> TYPE = new Type<>();

    public EditDoEvent(Id aDoToEdit){
        this.doToEdit = aDoToEdit;
    }

    @Override
    public Type<EditDoEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EditDoEventHandler handler) {
        handler.onEditDoEvent(this);
    }

    public Id getDoToEdit() {
        return doToEdit;
    }
}
