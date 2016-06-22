package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.GwtEvent;

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

    private static final Type<CustomDeleteEventHandler> TYPE = new Type<CustomDeleteEventHandler>();
    private CustomDeleteEventHandler.DeleteStatus status;
    private String message;

    public CustomDeleteEvent(CustomDeleteEventHandler.DeleteStatus status, String message){
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
}
