package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.07.2016
 * Time: 11:47
 * To change this template use File | Settings | File and Code Templates.
 */
public class ExpandHierarchyEvent extends GwtEvent<ExpandHierarchyEventHandler> {
    public static final Type<ExpandHierarchyEventHandler> TYPE = new Type<>();
    private Boolean expand;

    public ExpandHierarchyEvent(Boolean anExpand){
        expand = anExpand;
    }

    @Override
    public Type<ExpandHierarchyEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ExpandHierarchyEventHandler handler) {
        handler.onExpandHierarchyEvent(this);
    }

    public Boolean isExpand() {
        return expand;
    }
}
