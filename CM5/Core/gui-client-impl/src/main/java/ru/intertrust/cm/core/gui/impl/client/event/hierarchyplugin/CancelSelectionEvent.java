package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Событие сообщает о том что необходимо снять выделение со строки
 * коллекции. Параметр propagation указывает нужно ли распространять событие при получении
 * в родительский EventBus 
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 17.08.2016
 * Time: 15:26
 * To change this template use File | Settings | File and Code Templates.
 */
public class CancelSelectionEvent extends GwtEvent<CancelSelectionEventHandler> {

    public static final Type<CancelSelectionEventHandler> TYPE = new Type<>();

    private Boolean propagation = false;
    private Id rowId;

    public CancelSelectionEvent(Boolean aPropagation, Id aRowId){
        propagation = aPropagation;
        rowId = aRowId;
    }

    @Override
    public Type<CancelSelectionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CancelSelectionEventHandler handler) {
        handler.onCancelSelectionEvent(this);
    }
    public Boolean getPropagation() {
        return propagation;
    }

    public void setPropagation(Boolean propagation) {
        this.propagation = propagation;
    }

    public Id getRowId() {
        return rowId;
    }

    public void setRowId(Id rowId) {
        this.rowId = rowId;
    }
}
