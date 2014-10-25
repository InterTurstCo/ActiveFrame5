package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.2014
 *         Time: 7:25
 */
public class DomainObjectTypeSelectedEvent extends GwtEvent<DomainObjectTypeSelectedEventHandler> {

    public static GwtEvent.Type<DomainObjectTypeSelectedEventHandler> TYPE = new GwtEvent.Type<DomainObjectTypeSelectedEventHandler>();

    private String domainObjectType;

    public DomainObjectTypeSelectedEvent(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    @Override
    public GwtEvent.Type<DomainObjectTypeSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DomainObjectTypeSelectedEventHandler handler) {
        handler.onDomainObjectTypeSelected(this);
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }
}
