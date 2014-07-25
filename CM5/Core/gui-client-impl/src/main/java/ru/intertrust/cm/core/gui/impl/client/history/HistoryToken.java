package ru.intertrust.cm.core.gui.impl.client.history;

import java.util.HashMap;
import java.util.Map;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;

import ru.intertrust.cm.core.gui.api.client.history.HistoryItem;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 14:55.
 */
class HistoryToken {
    static final String UNKNOWN_LINK = "unknown";
    private static final String ASSIGN_KEY = "=";
    private static final String DELIMITER_KEY = ";";
    private static final String LINK_KEY = "link";

    private String link;
    private final Map<String, String> urlMap = new HashMap<>();
    private UserSettingsObject userSettingsObject = JavaScriptObject.createObject().cast();

    public HistoryToken(final String link) {
        this.link = link;
        final Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            userSettingsObject = loadSessionData(this);
        }
    }

    public String getLink() {
        return (link != null && !link.isEmpty()) ? link : UNKNOWN_LINK;
    }

    public void addItems(final String identifier, final HistoryItem... items) {
        boolean sessionDataChanged = false;
        for (HistoryItem item : items) {
            if (HistoryItem.Type.URL == item.getType()) {
                urlMap.put(item.getName(), item.getValue());
            } else {
                UserSettingsObject settings = (UserSettingsObject) userSettingsObject.getAttr(identifier);
                if (settings == null) {
                    settings = JavaScriptObject.createObject().cast();
                    userSettingsObject.setAttr(identifier, settings);
                }
                settings.setAttr(item.getName(), HistoryItemObject.createObject(item.getType().name(), item.getValue()));
                sessionDataChanged = true;
            }
        }
        if (sessionDataChanged && !UNKNOWN_LINK.equals(getLink())) {
            updateSessionData();
        }
    }

    public HistoryItem getItem(final String identifier, final String key) {
        final String urlValue = urlMap.get(key);
        if (urlValue != null) {
            return new HistoryItem(HistoryItem.Type.URL, key, urlValue);
        } else {
            final HistoryItem result;
            final UserSettingsObject settings = (UserSettingsObject) userSettingsObject.getAttr(identifier);
            if (settings != null && settings.getAttr(key) != null) {
                result = getHistoryItem(key, settings.getAttr(key));
            } else {
                result = null;
            }
            return result;
        }
    }

    /**
     * Возвращает все значения содержащиеся в url и все значения содержащиеся в {@link UserSettingsObject}
     * с типом {@link HistoryItem.Type#PLUGIN_CONDITION} и идентификатором, указанным во входном параметре.
     * @param identifier
     * @return
     */
    public Map<String, String> getItems(final String identifier) {
        final Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : urlMap.entrySet()) {
            if (!LINK_KEY.equals(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        final UserSettingsObject itemsObj = userSettingsObject.getAttr(identifier).cast();
        final JSONObject jsonObj = new JSONObject(itemsObj);
        for (String key : jsonObj.keySet()) {
            final HistoryItemObject historyItemObject = itemsObj.getAttr(key).cast();
            if (!HistoryItem.Type.USER_INTERFACE.name().equals(historyItemObject.getType())) {
                result.put(key, historyItemObject.getValue());
            }
        }
        return result;
    }

    public String getUrlToken() {
        final StringBuilder builder = new StringBuilder();
        if (!UNKNOWN_LINK.equals(getLink())) {
            builder.append(LINK_KEY).append(ASSIGN_KEY).append(link).append(DELIMITER_KEY);
        }
        for (Map.Entry<String, String> entry : urlMap.entrySet()) {
            if (entry.getValue() != null) {
                builder.append(entry.getKey()).append(ASSIGN_KEY).append(entry.getValue()).append(DELIMITER_KEY);
            }
        }
        return builder.length() == 0 ? "" : builder.toString();
    }

    public static HistoryToken getHistoryToken(final String urlToken) {
        final HistoryToken result = new HistoryToken(UNKNOWN_LINK);
        if (urlToken != null && !urlToken.isEmpty()) {
            final String[] items = urlToken.split(DELIMITER_KEY);
            for (String item : items) {
                final String[] itemData = item.split(ASSIGN_KEY);
                if (itemData.length == 2) {
                    if (LINK_KEY.equals(itemData[0])) {
                        result.link = itemData[1];
                    } else {
                        result.urlMap.put(itemData[0], itemData[1]);
                    }
                }
            }
        }
        final Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            result.userSettingsObject = loadSessionData(result);
        }
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(HistoryToken.class.getSimpleName())
                .append(": link=").append(link)
                .append(", urlMap=").append(urlMap)
                .toString();
    }

    private static UserSettingsObject loadSessionData(HistoryToken token) {
        UserSettingsObject result = JavaScriptObject.createObject().cast();
        final Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            final String storageData = storage.getItem(token.getLink());
            if (storageData != null) {
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

    private void updateSessionData() {
        final Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            final JSONObject jsonObj = new JSONObject(userSettingsObject);
            storage.setItem(link, jsonObj.toString());
        }
    }

    private static HistoryItem getHistoryItem(final String name, final JavaScriptObject object) {
        HistoryItemObject historyObject = object.cast();
        final HistoryItem.Type itemType = HistoryItem.Type.valueOf(historyObject.getType());
        return new HistoryItem(itemType, name, historyObject.getValue());
    }

    private static class HistoryItemObject extends JavaScriptObject {
        protected HistoryItemObject(){}

        public static native HistoryItemObject createObject(final String type, final String value) /*-{
            return {type: type, value: value};
        }-*/;

        public final native String getType() /*-{
            return this.type;
        }-*/;

        public final native String getValue() /*-{
            return this.value;
        }-*/;
    }
}
