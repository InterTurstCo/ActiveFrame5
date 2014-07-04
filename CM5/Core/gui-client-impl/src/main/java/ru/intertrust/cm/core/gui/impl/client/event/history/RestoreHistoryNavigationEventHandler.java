package ru.intertrust.cm.core.gui.impl.client.event.history;

import com.google.gwt.event.shared.EventHandler;

import ru.intertrust.cm.core.gui.model.history.HistoryToken;

/**
 * @author Sergey.Okolot
 *         Created on 03.07.2014 16:35.
 */
public interface RestoreHistoryNavigationEventHandler extends EventHandler {

    void restoreHistory(HistoryToken token);
}
