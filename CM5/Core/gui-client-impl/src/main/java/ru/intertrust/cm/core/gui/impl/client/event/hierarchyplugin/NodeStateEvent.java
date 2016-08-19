package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin.HierarchyPluginConstants;

/**
 * Событие генерируется когда узел разворачивается или сворачивается. Необходимо
 * для сохранения актуального статуса плагина в истории.
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 18.08.2016
 * Time: 15:10
 * To change this template use File | Settings | File and Code Templates.
 */
public class NodeStateEvent extends GwtEvent<NodeStateEventHandler> {
    public static final Type<NodeStateEventHandler> TYPE = new Type<>();
    private Boolean expanded;
    private String viewID;
    private String parentViewID;

    @Override
    public Type<NodeStateEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(NodeStateEventHandler handler) {
        handler.onNodeStateEvent(this);
    }

    public NodeStateEvent(Boolean isExpanded, String aViewId, String aParentViewId){
        expanded = isExpanded;
        viewID = aViewId;
        parentViewID = aParentViewId;
    }

    public Boolean isExpanded() {
        return expanded;
    }

    public String getViewID() {
        return viewID;
    }

    public String getParentViewID() {
        return parentViewID;
    }

    @Override
    public String toString(){
        return "Expanded: "+expanded+" view ID: "+viewID+" Parent View ID: "+parentViewID;
    }
}
