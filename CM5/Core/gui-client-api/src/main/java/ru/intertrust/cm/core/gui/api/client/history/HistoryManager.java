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

    void applyUrl();

    boolean isLinkEquals(String link);

    boolean hasLink();

    void setToken(String url);

    void setLink(String link);

    void setSelectedIds(Id... ids);

    List<Id> getSelectedIds();

    void addHistoryItems(String identifier, HistoryItem... items);

    String getValue(String identifier, String key);

    Map<String, String> getValues(String identifier);
}
