package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Sergey.Okolot
 *         Created on 24.07.2014 16:04.
 */
public class ComponentWidthChangedEvent extends GwtEvent<ComponentWidthChangedHandler> {

    public static final Type<ComponentWidthChangedHandler> TYPE = new Type<>();

    private final int width;
    private final Object component;

    public ComponentWidthChangedEvent(final Object component, final int width) {
        this.width = width;
        this.component = component;
    }

    @Override
    public Type<ComponentWidthChangedHandler> getAssociatedType() {
        return TYPE;
    }

    public int getWidth() {
        return width;
    }

    public Object getComponent() {
        return component;
    }

    @Override
    protected void dispatch(ComponentWidthChangedHandler handler) {
        handler.handleEvent(this);
    }
}
