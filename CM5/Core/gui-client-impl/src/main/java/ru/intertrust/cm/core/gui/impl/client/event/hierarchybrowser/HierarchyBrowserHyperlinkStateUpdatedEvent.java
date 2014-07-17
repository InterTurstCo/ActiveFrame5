package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.PopupPanel;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HierarchyBrowserHyperlinkStateUpdatedEvent extends GwtEvent<HierarchyBrowserHyperlinkStateUpdatedEventHandler> {

    public static Type<HierarchyBrowserHyperlinkStateUpdatedEventHandler> TYPE = new Type<HierarchyBrowserHyperlinkStateUpdatedEventHandler>();
    private Id id;
    private String collectionName;
    private PopupPanel popupPanel;

    public HierarchyBrowserHyperlinkStateUpdatedEvent(Id id, String collectionName, PopupPanel popupPanel) {
        this.id = id;
        this.collectionName = collectionName;
        this.popupPanel = popupPanel;
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

    public PopupPanel getPopupPanel() {
        return popupPanel;
    }
}
