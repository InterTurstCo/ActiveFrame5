package ru.intertrust.cm.core.gui.impl.client.event.history;

import com.google.gwt.event.shared.GwtEvent;

import ru.intertrust.cm.core.gui.model.history.HistoryToken;

/**
 * @author Sergey.Okolot
 *         Created on 03.07.2014 16:36.
 */
public class RestoreHistoryNavigationEvent extends GwtEvent<RestoreHistoryNavigationEventHandler> {
    public static final Type<RestoreHistoryNavigationEventHandler> TYPE = new Type<>();

    private final HistoryToken token;

    public RestoreHistoryNavigationEvent(final HistoryToken token) {
        this.token = token;
    }

    @Override
    public Type<RestoreHistoryNavigationEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(RestoreHistoryNavigationEventHandler handler) {
        handler.restoreHistory(token);
    }
}
