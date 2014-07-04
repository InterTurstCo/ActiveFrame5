package ru.intertrust.cm.core.gui.api.client;

import ru.intertrust.cm.core.gui.model.history.HistoryItem;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 18:11.
 */
public interface HistoryManager {

    String UNKNOWN_LINK = "unknown";

    void setLink(String link);

    void addHistoryItems(HistoryItem... items);

    <T> T getValue(String key);
}
