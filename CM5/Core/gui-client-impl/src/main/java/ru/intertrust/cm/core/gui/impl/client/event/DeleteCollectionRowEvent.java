package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * User: IPetrov
 * Date: 09.12.13
 * Time: 13:28
 * событие для удаления элемента коллекции
 */
public class DeleteCollectionRowEvent extends GwtEvent<DeleteCollectionRowEventHandler> {

    public static GwtEvent.Type<DeleteCollectionRowEventHandler> TYPE = new GwtEvent.Type<DeleteCollectionRowEventHandler>();
    // поле для объекта, который будет удаляться из коллекции
    private Id id;

    public DeleteCollectionRowEvent(Id id) {
        this.id = id;
    }

    @Override
    public GwtEvent.Type<DeleteCollectionRowEventHandler> getAssociatedType() {
        return TYPE;
    }
    @Override
    protected void dispatch(DeleteCollectionRowEventHandler handler) {
        handler.deleteCollectionRow(this);
    }

    public Id getId() {
        return id;
    }
}
