package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Sergey.Okolot
 *         Created on 24.07.2014 16:10.
 */
public class ComponentOrderChangedEvent extends GwtEvent<ComponentOrderChangedHandler> {
    public static final Type<ComponentOrderChangedHandler> TYPE = new Type();

    private final int fromOrder;
    private final int toOrder;
    private final Object component;


    public ComponentOrderChangedEvent(final Object component, final int fromOrder, final int toOrder) {
        this.fromOrder = fromOrder;
        this.toOrder = toOrder;
        this.component = component;
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

    @Override
    public Type<ComponentOrderChangedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ComponentOrderChangedHandler handler) {
        handler.handleEvent(this);
    }
}
