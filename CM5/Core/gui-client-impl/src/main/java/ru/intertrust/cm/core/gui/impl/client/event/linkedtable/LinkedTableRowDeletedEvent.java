package ru.intertrust.cm.core.gui.impl.client.event.linkedtable;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.form.widget.RowItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.10.2014
 *         Time: 18:04
 */
public class LinkedTableRowDeletedEvent extends GwtEvent<LinkedTableRowDeletedEventHandler> {
    public static final Type<LinkedTableRowDeletedEventHandler> TYPE = new Type<LinkedTableRowDeletedEventHandler>();
    private RowItem rowItem;
    private boolean tooltipContent;

    public LinkedTableRowDeletedEvent(RowItem rowItem, boolean tooltipContent) {
        this.rowItem = rowItem;
        this.tooltipContent = tooltipContent;
    }

    @Override
    public Type<LinkedTableRowDeletedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(LinkedTableRowDeletedEventHandler handler) {
        handler.onLinkedTableRowDeletedEvent(this);

    }

    public RowItem getRowItem() {
        return rowItem;
    }

    public boolean isTooltipContent() {
        return tooltipContent;
    }
}