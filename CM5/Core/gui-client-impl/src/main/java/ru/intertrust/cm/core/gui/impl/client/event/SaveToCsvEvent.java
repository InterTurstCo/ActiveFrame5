package ru.intertrust.cm.core.gui.impl.client.event;


import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 18.01.14
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public class SaveToCsvEvent extends GwtEvent<SaveToCsvEventHandler> {
    public static final Type<SaveToCsvEventHandler> TYPE = new Type<SaveToCsvEventHandler>();
    private final List<Id> selectedIds;

    public SaveToCsvEvent() {
        this(null);
    }

    public SaveToCsvEvent(List<Id> selectedIds) {
        this.selectedIds = (selectedIds != null && selectedIds.isEmpty()) ? null : selectedIds;
    }

    @Override
    public Type<SaveToCsvEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SaveToCsvEventHandler handler) {
        handler.saveToCsv(this);
    }

    public List<Id> getSelectedIds() {
        return selectedIds;
    }
}
