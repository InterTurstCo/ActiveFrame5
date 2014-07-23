package ru.intertrust.cm.core.gui.impl.client.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gwt.user.client.History;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.history.HistoryException;
import ru.intertrust.cm.core.gui.api.client.history.HistoryItem;
import ru.intertrust.cm.core.gui.api.client.history.HistoryManager;
import ru.intertrust.cm.core.gui.model.util.StringUtil;
import static ru.intertrust.cm.core.gui.model.util.UserInterfaceSettings.*;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 15:24.
 */
public class HistoryManagerImpl implements HistoryManager {

    private static final String ID_DELIMITER = ",";

    private HistoryToken current = new HistoryToken(HistoryToken.UNKNOWN_LINK);
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
    public void applyUrl() {
        History.newItem(current.getUrlToken(), false);
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
    public boolean hasLink() {
        return !HistoryToken.UNKNOWN_LINK.equals(current.getLink());
    }

    @Override
    public void setSelectedIds(final Id... ids) {
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
        final String idsAsStr = builder.length() == 0 ? null : builder.toString();
        addHistoryItems(null, new HistoryItem(HistoryItem.Type.URL, SELECTED_IDS_KEY, idsAsStr));
    }

    @Override
    public List<Id> getSelectedIds() {
        final List<Id> result = new ArrayList<>();
        final String idsAsStr = getValue(null, SELECTED_IDS_KEY);
        if (idsAsStr != null) {
            final String[] idStrArray = idsAsStr.split(ID_DELIMITER);
            for (String idStr : idStrArray) {
                final Id id = StringUtil.idFromString(idStr);
                if (id != null) {
                    result.add(id);
                } else {
                    throw new HistoryException("Неправильный формат Id " + idStr);
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
    public void addHistoryItems(final String identifier, final HistoryItem... items) {
        current.addItems(identifier, items);
        if (Mode.APPLY == mode) {
            History.newItem(current.getUrlToken(), false);
        }
    }

    @Override
    public String getValue(final String identifier, final String key) {
        final HistoryItem item = current.getItem(identifier, key);
        return item == null ? null : item.getValue();
    }

    @Override
    public Map<String, String> getValues(String identifier) {
        final Map<String, String> result = current.getItems(identifier);
        result.remove(SELECTED_IDS_KEY);
        return result;
    }
}
