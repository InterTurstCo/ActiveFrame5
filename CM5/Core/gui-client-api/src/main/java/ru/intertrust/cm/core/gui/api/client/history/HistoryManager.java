package ru.intertrust.cm.core.gui.api.client.history;

import java.util.List;
import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 18:11.
 */
public interface HistoryManager {

    enum Mode {WRITE, APPLY}

    HistoryManager setMode(Mode mode, String identifier);

    boolean isLinkEquals(String link);

    boolean hasLink();

    void setToken(String url);

    void setLink(String link);

    void setSelectedIds(Id... ids);

    List<Id> getSelectedIds();

    void addHistoryItems(HistoryItem... items);

    <T> T getValue(String key);

    Map<String, Object> getValues();
}
