package ru.intertrust.cm.core.gui.impl.client.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gwt.user.client.History;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.history.HistoryItem;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.impl.client.util.StringUtil;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 15:24.
 */
public class HistoryManagerImpl implements HistoryManager {

    private static final String IDS_KEY = "ids";
    private static final String ID_DELIMITER = ",";

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
    public void setLink(final String link) {
        if (!link.equals(current.getLink())) {
            current = new HistoryToken(link);
            if (Mode.APPLY == mode) {
                History.newItem(current.getUrlToken(), false);
            }
        }
    }

    @Override
    public void setSelectedIds(Id... ids) {
        final StringBuilder builder = new StringBuilder();
        if (ids != null && ids.length > 0) {
            boolean isFirst = true;
            for (Id id : ids) {
                if (id != null) {
                    if (!isFirst) {
                        builder.append(ID_DELIMITER);
                    }
                    isFirst = false;
                    builder.append(id.toStringRepresentation());
                }
            }
        }
        addHistoryItems(
                new HistoryItem(HistoryItem.Type.URL, IDS_KEY, builder.length() == 0 ? null : builder.toString()));
    }

    @Override
    public List<Id> getSelectedIds() {
        final List<Id> result = new ArrayList<>();
        final String idsAsStr = getValue(IDS_KEY);
        if (idsAsStr != null) {
            final String[] idStrArray = idsAsStr.split(ID_DELIMITER);
            for (String idStr : idStrArray) {
                final Id id = StringUtil.idFromString(idStr);
                if (id != null) {
                    result.add(id);
                }
            }
        }
        return result;
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

    @Override
    public Map<String, Object> getValues() {
        final Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, HistoryItem> entry : current.getItems().entrySet()) {
            if (entry.getValue() != null && entry.getValue().getValue() != null
                    && HistoryItem.Type.URL == entry.getValue().getType()) {
                if (IDS_KEY.equals(entry.getKey())) {
                    result.put(IDS_KEY, getSelectedIds());
                } else {
                    result.put(entry.getKey(), entry.getValue().getValue());
                }
            }
        }
        return result;
    }
}
