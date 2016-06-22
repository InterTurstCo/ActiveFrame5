package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Событие генерируемое компонентом кастомного удаления при завершении операции
 * (успешно или не успешно, причина и т.д.) Данное событие ожидается виджетом
 * TableViewer
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.06.2016
 * Time: 10:09
 * To change this template use File | Settings | File and Code Templates.
 */
public class CustomDeleteEvent extends GwtEvent<CustomDeleteEventHandler> {

    public  static final Type<CustomDeleteEventHandler> TYPE = new Type<CustomDeleteEventHandler>();
    private CustomDeleteEventHandler.DeleteStatus status;
    private String message;
    private Id deletedObject;

    public CustomDeleteEvent(Id doToDelete, CustomDeleteEventHandler.DeleteStatus status, String message){
        this.deletedObject = doToDelete;
        this.status = status;
        this.message = message;
    }

    public CustomDeleteEvent(CustomDeleteEventHandler.DeleteStatus status){
        this.status = status;
    }

    @Override
    public GwtEvent.Type<CustomDeleteEventHandler> getAssociatedType() {
        return TYPE;
    }
    @Override
    protected void dispatch(CustomDeleteEventHandler handler) {
        handler.onDelete(this);
    }

    public CustomDeleteEventHandler.DeleteStatus getStatus() {
        return status;
    }

    public void setStatus(CustomDeleteEventHandler.DeleteStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Id getDeletedObject() {
        return deletedObject;
    }

    public void setDeletedObject(Id deletedObject) {
        this.deletedObject = deletedObject;
    }
}
