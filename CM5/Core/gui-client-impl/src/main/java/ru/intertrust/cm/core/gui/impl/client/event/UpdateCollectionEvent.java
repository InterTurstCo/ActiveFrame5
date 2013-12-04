package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * User: IPetrov
 * Date: 04.12.13
 * Time: 11:05
 * класс события, которое генерируется при обновлении коллекции для (CollectionPluginView)
 */
public class UpdateCollectionEvent extends GwtEvent<UpdateCollectionEventHandler> {

    public static Type<UpdateCollectionEventHandler> TYPE = new Type<>();

    @Override
    public Type<UpdateCollectionEventHandler> getAssociatedType() {
        return TYPE;
    }
    @Override
    protected void dispatch(UpdateCollectionEventHandler handler) {
        handler.updateCollection(this);
    }
}
