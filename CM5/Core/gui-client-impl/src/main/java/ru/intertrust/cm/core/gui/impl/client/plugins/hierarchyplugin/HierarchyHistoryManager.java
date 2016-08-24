package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.AutoOpenEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.AutoOpenedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.AutoOpenedEventHandler;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyHistoryNode;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyPluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 22.08.2016
 * Time: 9:44
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyHistoryManager implements HierarchyPluginConstants, AutoOpenedEventHandler {

    private EventBus commonEventBus;
    private HierarchyHistoryNode loadedList;

    public void saveHistory(HierarchyPluginData pData) {
        Command command = new Command(SAVE_PLUGIN_HISTORY, PLUGIN_COMPONENT_NAME, pData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Something was going wrong while saving plugin history");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                GWT.log("Plugin history successfully saved.");
            }
        });
    }

    public HierarchyPluginData restoreHistory(HierarchyPluginData pData, final HierarchyPluginView pView, EventBus eventBus) {
        commonEventBus = eventBus;
        commonEventBus.addHandler(AutoOpenedEvent.TYPE, this);

        Command command = new Command(RESTORE_PLUGIN_HISTORY, PLUGIN_COMPONENT_NAME, pData);
        final HierarchyPluginData pluginData = pData;
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Something was going wrong while saving plugin history");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                HierarchyPluginData restoredData = (HierarchyPluginData) result;
                loadedList = restoredData.getOpenedNodeList();
                if (restoredData.getOpenedNodeList() != null) {
                    openAutomatically(restoredData.getOpenedNodeList());
                }
                GWT.log("History restored");
            }
        });

        return pluginData;
    }

    private void openAutomatically(HierarchyHistoryNode nodeList) {
        if (!nodeList.isOpened()) {
            commonEventBus.fireEvent(new AutoOpenEvent(nodeList.getNodeId()));
            return;
        }
        if (nodeList.getChildren().size() > 0) {
            expandChildren(nodeList.getChildren());
        }
    }

    private void expandChildren(Collection<HierarchyHistoryNode> aChildren) {
        for (HierarchyHistoryNode n : aChildren) {
            if (!n.isOpened()) {
                commonEventBus.fireEvent(new AutoOpenEvent(n.getNodeId()));
                break;
            } else if(n.getChildren().size()>0){
                expandChildren(n.getChildren());
            }
        }
    }

    @Override
    public void onAutoOpenedEvent(AutoOpenedEvent event) {
        if (loadedList.getNodeId().equals(event.getViewId())) {
            loadedList.setOpened(true);
        }
        markChildrenAsOpened(loadedList.getChildren(), event.getViewId());

        openAutomatically(loadedList);
    }

    private void markChildrenAsOpened(Collection<HierarchyHistoryNode> aChildren, String viewId) {
        for (HierarchyHistoryNode n : aChildren) {
            if (n.getNodeId().equals(viewId)) {
                n.setOpened(true);
                break;
            }
            if (n.getChildren().size() > 0) {
                markChildrenAsOpened(n.getChildren(), viewId);
            }
        }
    }
}
