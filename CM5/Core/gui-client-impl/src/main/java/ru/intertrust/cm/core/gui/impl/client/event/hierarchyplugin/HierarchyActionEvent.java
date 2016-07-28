package ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin.HierarchyPluginStaticData;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 28.07.2016
 * Time: 11:18
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyActionEvent extends GwtEvent<HierarchyActionEventHandler> {
    private HierarchyPluginStaticData.Actions action;
    public static final Type<HierarchyActionEventHandler> TYPE = new Type<>();


    public HierarchyActionEvent(HierarchyPluginStaticData.Actions anAction){
        action = anAction;
    }

    @Override
    public Type<HierarchyActionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyActionEventHandler handler) {
        handler.onHierarchyActionEvent(this);
    }

    public HierarchyPluginStaticData.Actions getAction() {
        return action;
    }
}
