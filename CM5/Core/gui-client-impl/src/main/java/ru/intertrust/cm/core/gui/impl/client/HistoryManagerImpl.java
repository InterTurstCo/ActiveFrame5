package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

import ru.intertrust.cm.core.gui.api.client.HistoryManager;
import ru.intertrust.cm.core.gui.model.history.HistoryItem;
import ru.intertrust.cm.core.gui.model.history.HistoryToken;

/**
 * @author Sergey.Okolot
 *         Created on 01.07.2014 15:24.
 */
public class HistoryManagerImpl implements HistoryManager {

    private HistoryToken current = new HistoryToken("unknown");

    public HistoryManagerImpl() {
        History.addValueChangeHandler(new ValueChangeHandlerImpl());
    }

    public void addHistoryItems(HistoryItem... items) {
        current.addItems(items);
        History.newItem(current.getUrlToken(), false);
    }

    @Override
    public <T> T getValue(String key) {
        final HistoryItem item = current.getItem(key);
        return item == null ? null : (T) item.getValue();
    }

    private class ValueChangeHandlerImpl implements ValueChangeHandler<String> {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
            System.out.println("-------------->> I'm here " + event.getValue() + " ------------ " + event.getSource());
        }
    }
}
