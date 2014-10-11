package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser.HierarchyBrowserHyperlinkDisplay;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HierarchyBrowserHyperlinkStateUpdatedEvent extends GwtEvent<HierarchyBrowserHyperlinkStateUpdatedEventHandler> {

    public static Type<HierarchyBrowserHyperlinkStateUpdatedEventHandler> TYPE = new Type<HierarchyBrowserHyperlinkStateUpdatedEventHandler>();
    private Id id;
    private String collectionName;
    private HierarchyBrowserHyperlinkDisplay hyperlinkDisplay;
    private boolean tooltipContent;

    public HierarchyBrowserHyperlinkStateUpdatedEvent(Id id, String collectionName,
                                                      HierarchyBrowserHyperlinkDisplay hyperlinkDisplay, boolean tooltipContent) {
        this.id = id;
        this.collectionName = collectionName;
        this.hyperlinkDisplay = hyperlinkDisplay;
        this.tooltipContent = tooltipContent;
    }

    @Override
    public Type<HierarchyBrowserHyperlinkStateUpdatedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserHyperlinkStateUpdatedEventHandler handler) {
        handler.onHierarchyBrowserHyperlinkStateUpdatedEvent(this);
    }

    public Id getId() {
        return id;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public HierarchyBrowserHyperlinkDisplay getHyperlinkDisplay() {
        return hyperlinkDisplay;
    }

    public boolean isTooltipContent() {
        return tooltipContent;
    }
}
