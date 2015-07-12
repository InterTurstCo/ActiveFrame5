package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Sergey.Okolot
 *         Created on 24.07.2014 16:10.
 */
public class ComponentOrderChangedEvent extends GwtEvent<ComponentOrderChangedHandler> {
    public static final Type<ComponentOrderChangedHandler> TYPE = new Type();

    private int fromOrder;
    private int toOrder;
    private Object component;

    private Column movedByUser;
    private Column evicted;

    @Deprecated //use ComponentOrderChangedEvent(Column movedByUser, Column evicted) instead
    public ComponentOrderChangedEvent(final Object component, final int fromOrder, final int toOrder) {
        this.fromOrder = fromOrder;
        this.toOrder = toOrder;
        this.component = component;
    }

    public ComponentOrderChangedEvent(Column movedByUser, Column evicted) {
        this.movedByUser = movedByUser;
        this.evicted = evicted;
    }

    public int getFromOrder() {
        return fromOrder;
    }

    public int getToOrder() {
        return toOrder;
    }

    public Object getComponent() {
        return component;
    }

    public Column getMovedByUser() {
        return movedByUser;
    }

    public Column getEvicted() {
        return evicted;
    }

    @Override
    public Type<ComponentOrderChangedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ComponentOrderChangedHandler handler) {
        handler.handleEvent(this);
    }
}
