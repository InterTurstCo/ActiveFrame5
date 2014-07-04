package ru.intertrust.cm.core.gui.impl.client.history;

import com.google.gwt.user.client.History;

import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.api.client.history.HistoryItem;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 15:24.
 */
public class HistoryManagerImpl implements HistoryManager {

    private HistoryToken current = new HistoryToken(UNKNOWN_LINK);

    @Override
    public void setToken(String url) {
        current = HistoryToken.getHistoryToken(url);
    }

    @Override
    public void setLink(String link) {
        if (!link.equals(current.getLink())) {
            current = new HistoryToken(link);
            History.newItem(current.getUrlToken(), false);
        }
    }

    @Override
    public String getLink() {
        return current.getLink();
    }

    @Override
    public void addHistoryItems(HistoryItem... items) {
        current.addItems(items);
        History.newItem(current.getUrlToken(), false);
    }

    @Override
    public <T> T getValue(String key) {
        final HistoryItem item = current.getItem(key);
        return item == null ? null : (T) item.getValue();
    }
}
