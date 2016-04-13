package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 06.04.2016
 * Time: 16:41
 * To change this template use File | Settings | File and Code Templates.
 */
public class BreadCrumbNavigationEvent extends GwtEvent<BreadCrumbNavigationEventHandler> {
    public static Type<BreadCrumbNavigationEventHandler> TYPE = new Type<BreadCrumbNavigationEventHandler>();
    @Override
    public Type<BreadCrumbNavigationEventHandler> getAssociatedType() {
        return TYPE;
    }
    @Override
    protected void dispatch(BreadCrumbNavigationEventHandler handler) {
        handler.onNavigation(this);
    }
}
