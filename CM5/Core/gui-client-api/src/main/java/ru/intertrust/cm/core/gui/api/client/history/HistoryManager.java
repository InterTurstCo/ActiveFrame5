package ru.intertrust.cm.core.gui.api.client.history;

import ru.intertrust.cm.core.gui.api.client.history.HistoryItem;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 18:11.
 */
public interface HistoryManager {

    String UNKNOWN_LINK = "unknown";

    void setToken(String url);

    void setLink(String link);

    String getLink();

    void addHistoryItems(HistoryItem... items);

    <T> T getValue(String key);
}
