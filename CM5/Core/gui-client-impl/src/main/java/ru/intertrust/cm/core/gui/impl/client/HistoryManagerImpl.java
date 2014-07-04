package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.event.history.RestoreHistoryNavigationEvent;
import ru.intertrust.cm.core.gui.model.history.HistoryItem;
import ru.intertrust.cm.core.gui.model.history.HistoryToken;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 15:24.
 */
public class HistoryManagerImpl implements HistoryManager {

    private HistoryToken current = new HistoryToken(UNKNOWN_LINK);

    public HistoryManagerImpl() {
        History.addValueChangeHandler(new ValueChangeHandlerImpl());
    }

    @Override
    public void setLink(String link) {
        current = new HistoryToken(link);
        History.newItem(current.getUrlToken(), false);
    }

    public void addHistoryItems(HistoryItem... items) {
        current.addItems(items);
        History.newItem(current.getUrlToken(), false);
    }

    @Override
    public <T> T getValue(String key) {
        final HistoryItem item = current.getItem(key);
        return item == null ? null : (T) item.getValue();
    }

    private class ValueChangeHandlerImpl implements ValueChangeHandler<String> {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
            final HistoryToken restoreToken = HistoryToken.getHistoryToken(event.getValue());
            boolean linkChanged = restoreToken.getLink() != null
                    && !restoreToken.getLink().isEmpty()
                    && !UNKNOWN_LINK.equals(restoreToken.getLink())
                    && !restoreToken.getLink().equals(current.getLink());
            current = restoreToken;
            if (linkChanged) {
                final RestoreHistoryNavigationEvent restoreHistoryNavigationEvent =
                        new RestoreHistoryNavigationEvent(restoreToken);
                Application.getInstance().getEventBus().fireEvent(restoreHistoryNavigationEvent);
            }
        }
    }
}
