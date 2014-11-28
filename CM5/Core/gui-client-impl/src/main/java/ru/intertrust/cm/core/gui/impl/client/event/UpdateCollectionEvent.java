package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;

/**
 * User: IPetrov
 * Date: 04.12.13
 * Time: 11:05
 * класс события, которое генерируется при обновлении коллекции для (CollectionPluginView)
 */
public class UpdateCollectionEvent extends GwtEvent<UpdateCollectionEventHandler> {

    public static Type<UpdateCollectionEventHandler> TYPE = new Type<>();
    // поле для объекта, который будет добавляться в коллекцию
    private IdentifiableObject identifiableObject;
    private Id id;

    public UpdateCollectionEvent(IdentifiableObject identifiableObject) {
        this.identifiableObject = identifiableObject;
    }

    public UpdateCollectionEvent(Id id) {
        this.id = id;
    }

    @Override
    public Type<UpdateCollectionEventHandler> getAssociatedType() {
        return TYPE;
    }
    @Override
    protected void dispatch(UpdateCollectionEventHandler handler) {
        handler.updateCollection(this);
    }

    public IdentifiableObject getIdentifiableObject() {
        return identifiableObject;
    }

    public Id getId() {
        return id;
    }
}
