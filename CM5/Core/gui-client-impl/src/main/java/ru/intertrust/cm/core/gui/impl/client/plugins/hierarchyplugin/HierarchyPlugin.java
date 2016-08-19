package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyPluginConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.NodeCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.NodeCreatedEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.NodeStateEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.NodeStateEventHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyHistoryNode;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyPluginData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 26.07.2016
 * Time: 14:20
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("hierarchy.plugin")
public class HierarchyPlugin extends Plugin implements NodeStateEventHandler, NodeCreatedEventHandler {

    private EventBus eventBus = new SimpleEventBus();
    private Map<String, Boolean> existedNodeList = new HashMap<>();
    private HierarchyHistoryNode openedNodeList;

    @Override
    public PluginView createView() {
        HierarchyPluginConfig hierarchyPluginConfig = (HierarchyPluginConfig)getConfig();
        return new HierarchyPluginView(this, eventBus);
    }

    public HierarchyPlugin(){
        super();
        eventBus.addHandler(NodeStateEvent.TYPE, this);
        eventBus.addHandler(NodeCreatedEvent.TYPE, this);
    }

    @Override
    public Component createNew() {
        return new HierarchyPlugin();
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void onNodeStateEvent(NodeStateEvent event) {
        HierarchyPluginData pData = new HierarchyPluginData();
        if(event.isExpanded()){
            if(openedNodeList == null){
                openedNodeList = new HierarchyHistoryNode(event.getViewID(), event.getParentViewID());
            } else {
                openedNodeList.add(new HierarchyHistoryNode(event.getViewID(), event.getParentViewID()));
            }
        } else {
            openedNodeList.remove(new HierarchyHistoryNode(event.getViewID(), event.getParentViewID()));
        }

        pData.setOpenedNodeList(openedNodeList);
    }

    @Override
    public void onNodeCreatedEvent(NodeCreatedEvent event) {
        existedNodeList.put(event.getViewId(),true);
    }
}
