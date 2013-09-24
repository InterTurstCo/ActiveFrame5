package ru.intertrust.cm.core.gui.impl.client.plugins.navigation;

import com.google.gwt.event.shared.GwtEvent;

public class RootLinkSelectedEvent extends GwtEvent<RootNodeSelectedEventHandler> {

    private String selectedRootLinkName;

    public static Type<RootNodeSelectedEventHandler> TYPE = new Type<RootNodeSelectedEventHandler>();

    public RootLinkSelectedEvent(String selectedRootLinkName) {
        this.selectedRootLinkName = selectedRootLinkName;
    }

    @Override
    public Type<RootNodeSelectedEventHandler> getAssociatedType() {
        return TYPE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void dispatch(RootNodeSelectedEventHandler handler) {
        handler.onRootNodeSelected(this);
    }

    public String getSelectedRootLinkName() {
        return selectedRootLinkName;
    }
}
