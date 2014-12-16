package ru.intertrust.cm.core.gui.impl.client.event.form;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.12.2014
 *         Time: 19:48
 */
public class ParentTabSelectedEvent extends GwtEvent<ParentTabSelectedEventHandler> {
    public static Type<ParentTabSelectedEventHandler> TYPE = new Type<ParentTabSelectedEventHandler>();
    private Widget parent;
    public ParentTabSelectedEvent(Widget parent) {
        this.parent = parent;
    }

    @Override
    public Type<ParentTabSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ParentTabSelectedEventHandler handler) {
        handler.onParentTabSelectedEvent(this);
    }

    public Widget getParent() {
        return parent;
    }
}
