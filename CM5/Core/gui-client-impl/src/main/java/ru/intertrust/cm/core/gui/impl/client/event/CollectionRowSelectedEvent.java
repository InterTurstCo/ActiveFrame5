package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

public class CollectionRowSelectedEvent extends GwtEvent<CollectionRowSelectedEventHandler> {

    public static Type<CollectionRowSelectedEventHandler> TYPE = new Type<CollectionRowSelectedEventHandler>();
    private Id id;
    /**
     * Нужно ли производить скролл до выделенного элемента коллекции. Флаг нужен для различения событий автоматического (кодом) и ручного выделения элемента.
     */
    private boolean scrollToSelectedRow = false;

    public CollectionRowSelectedEvent(Id id) {
        this.id = id;
    }

    public CollectionRowSelectedEvent(Id id, boolean scrollToSelectedRow) {
        this.id = id;
        this.scrollToSelectedRow = scrollToSelectedRow;
    }

    @Override
    public Type<CollectionRowSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionRowSelectedEventHandler handler) {
        handler.onCollectionRowSelect(this);
    }

    public Id getId() {
        return id;
    }

    public boolean isScrollToSelectedRow() {
        return scrollToSelectedRow;
    }

}
