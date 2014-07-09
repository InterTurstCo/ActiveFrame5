package ru.intertrust.cm.core.gui.api.client.history;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 18:11.
 */
public interface HistoryManager {

    enum Mode {WRITE, APPLY}

    HistoryManager setMode(Mode mode, String identifier);

    boolean isLinkEquals(String link);

    void setToken(String url);

    void setLink(String link);

    void addHistoryItems(HistoryItem... items);

    <T> T getValue(String key);
}
