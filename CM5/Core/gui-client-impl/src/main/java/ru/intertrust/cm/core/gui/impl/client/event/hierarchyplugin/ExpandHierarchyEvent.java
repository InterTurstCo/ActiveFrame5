package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

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
    private Id parentId;
    private Boolean autoClick;

    public ExpandHierarchyEvent(Boolean anExpand, Id aParentId, Boolean isAutoClick){
        expand = anExpand;
        parentId = aParentId;
        autoClick = isAutoClick;
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

    public Id getParentId() {
        return parentId;
    }

    public void setParentId(Id parentId) {
        this.parentId = parentId;
    }

    public Boolean isAutoClick() {
        return autoClick;
    }
}
