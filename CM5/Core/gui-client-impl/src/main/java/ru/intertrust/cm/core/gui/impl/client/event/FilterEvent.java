package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.06.2014
 *         Time: 1:13
 */
public class FilterEvent extends GwtEvent<FilterEventHandler> {
    public static final Type<FilterEventHandler> TYPE = new Type<FilterEventHandler>();
    private boolean filterCanceled;
    private boolean remote;

    public FilterEvent(boolean filterCanceled) {
        this.filterCanceled = filterCanceled;
    }

    public FilterEvent(boolean filterCanceled, boolean remote) {
        this.filterCanceled = filterCanceled;
        this.remote = remote;
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

    public boolean isRemote() {
        return remote;
    }

}
