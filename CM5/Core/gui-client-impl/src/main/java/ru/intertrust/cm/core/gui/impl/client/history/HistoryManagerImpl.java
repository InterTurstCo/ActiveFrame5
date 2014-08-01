package ru.intertrust.cm.core.gui.impl.client.history;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.History;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.history.HistoryException;
import ru.intertrust.cm.core.gui.api.client.history.HistoryItem;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.model.util.StringUtil;

import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.ASSIGN_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.DELIMITER_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.LINK_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.SELECTED_IDS_KEY;
import static ru.intertrust.cm.core.gui.model.util.UserSettingsHelper.UNKNOWN_LINK;

/**
 *
 * @author Sergey.Okolot
 *         Created on 30.07.2014 19:16.
 */
public class HistoryManagerImpl implements HistoryManager {

    private String link = UNKNOWN_LINK;
    private Mode mode = Mode.APPLY;
    private String identifier;
    private final Map<String, String> urlMap = new HashMap<>();
    private UserSettingsObject sessionData = UserSettingsObject.createObject().cast();

    @Override
    public HistoryManager setMode(Mode mode, String identifier) {
        if (Mode.WRITE == mode) {
            if (this.identifier == null) {
                this.mode = mode;
                this.identifier = identifier;
            }
        } else if (Mode.APPLY == mode) {
            if (this.identifier == null || this.identifier.equals(identifier)) {
                this.mode = mode;
                this.identifier = null;
                History.newItem(getUrlToken(), false);
            }
        }
        return this;
    }

    @Override
    public void applyUrl() {
        History.newItem(getUrlToken(), false);
    }

    @Override
    public boolean hasLink() {
        return !UNKNOWN_LINK.equals(link);
    }

    @Override
    public void setToken(final String url) {
        link = UNKNOWN_LINK;
        if (url != null && !url.isEmpty()) {
            final String[] items = url.split(DELIMITER_KEY);
            for (String item : items) {
                final String[] itemData = item.split(ASSIGN_KEY);
                if (itemData.length == 2) {
                    if (LINK_KEY.equals(itemData[0])) {
                        link = itemData[1];
                    } else {
                        urlMap.put(itemData[0], itemData[1]);
                    }
                }
            }
        }
        sessionData = loadSessionData();
    }

    @Override
    public void setLink(String link) {
        if (!this.link.equals(link)) {
            this.link = link;
            if (Mode.APPLY == mode) {
                History.newItem(getUrlToken(), false);
            }
        }
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public void setSelectedIds(final Id... ids) {
        final String selectedIds = StringUtil.idArrayToStr(ids);
        urlMap.put(SELECTED_IDS_KEY, selectedIds);
        if (Mode.APPLY == mode) {
            History.newItem(getUrlToken(), false);
        }
    }

    @Override
    public List<Id> getSelectedIds() {
        try {
            return StringUtil.idsStrToList(urlMap.get(SELECTED_IDS_KEY));
        } catch (Exception ex) {
            throw new HistoryException(ex.getMessage());
        }
    }

    @Override
    public void addHistoryItems(final String identifier, final HistoryItem... items) {
        boolean sessionDataChanged = false;
        for (HistoryItem item : items) {
            if (HistoryItem.Type.URL == item.getType()) {
                urlMap.put(item.getName(), item.getValue());
            } else if (HistoryItem.Type.USER_INTERFACE == item.getType()) {
                sessionDataChanged = true;
                UserSettingsObject settingsObject = sessionData.getAttr(identifier).cast();
                if (settingsObject == null) {
                    settingsObject = UserSettingsObject.createObject().cast();
                    sessionData.setAttr(identifier, settingsObject);
                }
                final HistoryItemObject itemObject =
                        HistoryItemObject.createObject(item.getType().name(), item.getValue());
                settingsObject.setAttr(item.getName(), itemObject);
            }
        }
        if (sessionDataChanged) {
            Storage storage = Storage.getSessionStorageIfSupported();
            if (storage != null) {
                storage.setItem(link, new JSONObject(sessionData).toString());
            }
        }
        if (Mode.APPLY == mode) {
            History.newItem(getUrlToken(), false);
        }
    }

    @Override
    public String getValue(String identifier, String key) {
        String result;
        result = urlMap.get(key);
        if (result == null) {
            HistoryItemObject itemObject = sessionData.getAttr(identifier).cast();
            if (itemObject != null) {
                result = itemObject.getValue();
            }
        }
        return result;
    }

    @Override
    public Map<String, String> getValues(String identifier) {
        return urlMap;
    }

    @Override
    public String toString() {
        return new StringBuilder(HistoryManagerImpl.class.getSimpleName())
                .append(": link=").append(link)
                .append(", urlMap=").append(urlMap)
                .toString();
    }

    private String getUrlToken() {
        final StringBuilder builder = new StringBuilder();
        if (!UNKNOWN_LINK.equals(link)) {
            builder.append(LINK_KEY).append(ASSIGN_KEY).append(link).append(DELIMITER_KEY);
        }
        for (Map.Entry<String, String> entry : urlMap.entrySet()) {
            if (entry.getValue() != null) {
                builder.append(entry.getKey()).append(ASSIGN_KEY).append(entry.getValue()).append(DELIMITER_KEY);
            }
        }
        return builder.toString();
    }

    private UserSettingsObject loadSessionData() {
        UserSettingsObject result = UserSettingsObject.createObject().cast();
        final Storage storage = Storage.getSessionStorageIfSupported();
        if (storage != null) {
            final String storageData = storage.getItem(link);
            if (storageData != null && !storageData.isEmpty()) {
                try {
                    JSONValue jsonValue = JSONParser.parseStrict(storageData);
                    if (jsonValue != null) {
                        result = jsonValue.isObject().getJavaScriptObject().cast();
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }
}
