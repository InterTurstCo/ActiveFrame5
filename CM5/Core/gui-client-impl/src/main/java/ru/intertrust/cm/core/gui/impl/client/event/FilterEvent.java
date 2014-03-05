package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by User on 03.03.14.
 */
public class FilterEvent extends GwtEvent<FilterEventHandler> {
    public static final Type<FilterEventHandler> TYPE = new Type<FilterEventHandler>();
    private boolean filterCanceled;

    public FilterEvent(boolean filterCanceled) {
        this.filterCanceled = filterCanceled;
    }

    @Override
    public Type<FilterEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FilterEventHandler handler) {
        handler.onFilterEvent(this);
    }

    public boolean isFilterCanceled() {
        return filterCanceled;
    }
}
