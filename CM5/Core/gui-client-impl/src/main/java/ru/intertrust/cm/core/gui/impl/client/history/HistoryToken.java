package ru.intertrust.cm.core.gui.impl.client.history;

import java.util.HashMap;
import java.util.Map;

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
    }

    public String getLink() {
        return (link != null && !link.isEmpty()) ? link : UNKNOWN_LINK;
    }

    public void addItems(HistoryItem... items) {
        for (HistoryItem item : items) {
            itemMap.put(item.getName(), item);
        }
    }

    public HistoryItem getItem(final String key) {
        return itemMap.get(key);
    }

    public String getUrlToken() {
        final StringBuilder builder = new StringBuilder(LINK_KEY).append(ASSIGN_KEY).append(link);
        for (Map.Entry<String, HistoryItem> entry : itemMap.entrySet()) {
            if (HistoryItem.Type.SESSION != entry.getValue().getType() && entry.getValue().getValue() != null) {
                builder.append(DELIMITER_KEY).append(entry.getKey()).append(ASSIGN_KEY)
                        .append(entry.getValue().getValue());
            }
        }
        return builder.toString();
    }

    public static HistoryToken getHistoryToken(final String urlToken) {
        final HistoryToken token = new HistoryToken();
        if (urlToken != null && !urlToken.isEmpty()) {
            final String[] items = urlToken.split(DELIMITER_KEY);
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
        return token;
    }

    @Override
    public String toString() {
        return new StringBuilder(HistoryToken.class.getSimpleName())
                .append(": link=").append(link)
                .append(", itemMap=").append(itemMap)
                .toString();
    }
}
