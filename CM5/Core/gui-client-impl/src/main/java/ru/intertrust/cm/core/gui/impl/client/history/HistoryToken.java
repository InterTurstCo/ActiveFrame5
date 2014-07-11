package ru.intertrust.cm.core.gui.impl.client.history;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, HistoryItem> itemMap = new HashMap<>();

    public HistoryToken() {
        this(UNKNOWN_LINK);
    }

    public HistoryToken(final String link) {
        this.link = link;
        final Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            final String storageToken = storage.getItem(link);
            parseTokenString(storageToken, this);
        }
    }

    public String getLink() {
        return (link != null && !link.isEmpty()) ? link : UNKNOWN_LINK;
    }

    public void addItems(HistoryItem... items) {
        boolean sessionDataChanged = false;
        for (HistoryItem item : items) {
            itemMap.put(item.getName(), item);
            if (HistoryItem.Type.SESSION == item.getType()) {
                sessionDataChanged = true;
            }
        }
        if (sessionDataChanged && !UNKNOWN_LINK.equals(getLink())) {
            final Storage storage = Storage.getLocalStorageIfSupported();
            if (storage != null) {
                final String storageToken = tokenToStringByType(HistoryItem.Type.SESSION, this);
                storage.setItem(getLink(), storageToken);
            }
        }
    }

    public Map<String, HistoryItem> getItems() {
        return Collections.unmodifiableMap(itemMap);
    }

    public HistoryItem getItem(final String key) {
        return itemMap.get(key);
    }

    public String getUrlToken() {
        final StringBuilder builder = new StringBuilder();
        if (!UNKNOWN_LINK.equals(getLink())) {
            builder.append(LINK_KEY).append(ASSIGN_KEY).append(link).append(DELIMITER_KEY);
        }
        builder.append(tokenToStringByType(HistoryItem.Type.URL, this));
        return builder.toString();
    }

    public static HistoryToken getHistoryToken(final String urlToken) {
        final HistoryToken token = new HistoryToken();
        parseTokenString(urlToken, token);
        final Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            final String storageToken = storage.getItem(token.getLink());
            parseTokenString(storageToken, token);
        }
        return token;
    }

    @Override
    public String toString() {
        return new StringBuilder(HistoryToken.class.getSimpleName())
                .append(": link=").append(link)
                .append(", itemMap=").append(itemMap)
                .toString();
    }

    private static void parseTokenString(final String tokenAsStr, final HistoryToken token) {
        if (tokenAsStr != null && !tokenAsStr.isEmpty()) {
            final String[] items = tokenAsStr.split(DELIMITER_KEY);
            for (String item : items) {
                final String[] itemData = item.split(ASSIGN_KEY);
                if (itemData.length == 2) {
                    if (LINK_KEY.equals(itemData[0])) {
                        token.link = itemData[1];
                    } else {
                        token.itemMap.put(itemData[0], new HistoryItem(HistoryItem.Type.URL, itemData[0], itemData[1]));
                    }
                }
            }
        }
    }

    private static String tokenToStringByType(final HistoryItem.Type type, final HistoryToken token) {
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, HistoryItem> entry : token.itemMap.entrySet()) {
            if (type == entry.getValue().getType() && entry.getValue().getValue() != null) {
                builder.append(DELIMITER_KEY).append(entry.getKey()).append(ASSIGN_KEY)
                        .append(entry.getValue().getValue());
            }
        }
        return builder.length() == 0 ? "" : builder.substring(1);
    }
}
