package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser.HierarchyBrowserDisplay;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserItemClickEvent extends GwtEvent<HierarchyBrowserItemClickEventHandler> {

    public static Type<HierarchyBrowserItemClickEventHandler> TYPE = new Type<HierarchyBrowserItemClickEventHandler>();

    private HierarchyBrowserItem item;
    private HierarchyBrowserDisplay hyperlinkDisplay;
    private boolean tooltipContent;

    public HierarchyBrowserItemClickEvent(HierarchyBrowserItem item, HierarchyBrowserDisplay hyperlinkDisplay,
                                          boolean tooltipContent) {
        this.item = item;
        this.hyperlinkDisplay = hyperlinkDisplay;
        this.tooltipContent = tooltipContent;
    }

    @Override
    public Type<HierarchyBrowserItemClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserItemClickEventHandler handler) {
        handler.onHierarchyBrowserItemClick(this);
    }

    public HierarchyBrowserItem getItem() {
        return item;
    }

    public boolean isTooltipContent() {
        return tooltipContent;
    }

    public HierarchyBrowserDisplay getHyperlinkDisplay() {
        return hyperlinkDisplay;
    }
}
