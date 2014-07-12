package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser.HierarchyBrowserItemsView;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.07.2014
 *         Time: 22:16
 */
public class HierarchyBrowserShowTooltipEvent extends GwtEvent<HierarchyBrowserShowTooltipEventHandler> {
    private HierarchyBrowserItemsView itemsView;

    public HierarchyBrowserShowTooltipEvent(HierarchyBrowserItemsView itemsView) {
        this.itemsView = itemsView;
    }

    public static Type<HierarchyBrowserShowTooltipEventHandler> TYPE = new Type<HierarchyBrowserShowTooltipEventHandler>();

    @Override
    public Type<HierarchyBrowserShowTooltipEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserShowTooltipEventHandler handler) {
        handler.onHierarchyBrowserShowTooltip(this);
    }

    public HierarchyBrowserItemsView getItemsView() {
        return itemsView;
    }
}
