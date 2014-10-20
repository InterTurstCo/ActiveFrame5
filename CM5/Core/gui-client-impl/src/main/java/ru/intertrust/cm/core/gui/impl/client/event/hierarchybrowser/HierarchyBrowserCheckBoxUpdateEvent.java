package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserCheckBoxUpdateEvent extends GwtEvent<HierarchyBrowserCheckBoxUpdateEventHandler> {

    public static Type<HierarchyBrowserCheckBoxUpdateEventHandler> TYPE = new Type<HierarchyBrowserCheckBoxUpdateEventHandler>();
    private HierarchyBrowserItem item;
    private HierarchyBrowserItem previousChosenItem;
    public HierarchyBrowserCheckBoxUpdateEvent(HierarchyBrowserItem item, HierarchyBrowserItem previousSelectedItem) {
        this.item = item;
        this.previousChosenItem = previousSelectedItem;
    }

    @Override
    public Type<HierarchyBrowserCheckBoxUpdateEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserCheckBoxUpdateEventHandler handler) {
        handler.onHierarchyBrowserCheckBoxUpdate(this);
    }

    public HierarchyBrowserItem getItem() {
        return item;
    }

    public HierarchyBrowserItem getPreviousChosenItem() {
        return previousChosenItem;
    }
}