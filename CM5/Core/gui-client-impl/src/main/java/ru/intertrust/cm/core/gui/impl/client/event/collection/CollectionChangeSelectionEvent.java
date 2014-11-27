package ru.intertrust.cm.core.gui.impl.client.event.collection;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.11.2014
 *         Time: 6:55
 */
public class CollectionChangeSelectionEvent extends GwtEvent<CollectionChangeSelectionEventHandler> {
    public static Type<CollectionChangeSelectionEventHandler> TYPE = new Type<CollectionChangeSelectionEventHandler>();
    private List<Id> id;
    private boolean selected;

    public CollectionChangeSelectionEvent(List<Id> id, boolean selected) {
        this.id = id;
        this.selected = selected;
    }

    public void setId(List<Id> id) {
        this.id = id;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public Type<CollectionChangeSelectionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CollectionChangeSelectionEventHandler handler) {
        handler.changeCollectionSelection(this);
    }

    public List<Id> getId() {
        return id;
    }

    public boolean isSelected() {
        return selected;
    }
}
