package ru.intertrust.cm.core.gui.impl.client.history;

import com.google.gwt.user.client.History;

import ru.intertrust.cm.core.gui.api.client.history.HistoryItem;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 15:24.
 */
public class HistoryManagerImpl implements HistoryManager {

    private HistoryToken current = new HistoryToken();
    private Mode mode = Mode.APPLY;
    private String identifier;

    @Override
    public HistoryManager setMode(final Mode mode, final String identifier) {
            if (Mode.WRITE == mode) {
                if (this.identifier == null) {
                    this.mode = mode;
                    this.identifier = identifier;
                }
            } else if (Mode.APPLY == mode) {
                if (this.identifier == null || this.identifier.equals(identifier)) {
                    this.mode = mode;
                    this.identifier = null;
                    History.newItem(current.getUrlToken(), false);
                }
            }
        return this;
    }

    @Override
    public void setToken(String url) {
        current = HistoryToken.getHistoryToken(url);
    }

    @Override
    public void setLink(String link) {
        if (!link.equals(current.getLink())) {
            current = new HistoryToken(link);
            if (Mode.APPLY == mode) {
                History.newItem(current.getUrlToken(), false);
            }
        }
    }

    @Override
    public boolean isLinkEquals(final String link) {
        return current.getLink().equals(link);
    }

    @Override
    public void addHistoryItems(HistoryItem... items) {
        current.addItems(items);
        if (Mode.APPLY == mode) {
            History.newItem(current.getUrlToken(), false);
        }
    }

    @Override
    public <T> T getValue(String key) {
        final HistoryItem item = current.getItem(key);
        return item == null ? null : (T) item.getValue();
    }
}
