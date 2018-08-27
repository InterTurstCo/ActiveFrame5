package ru.intertrust.cm.core.gui.impl.client.plugins.cluster;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;
import ru.intertrust.cm.core.gui.impl.client.plugins.globalcache.GlobalCacheControlUtils;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.cluster.ClusterManagementPluginData;
import ru.intertrust.cm.core.gui.model.plugin.cluster.ClusterNodeInfoViewModel;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;


import java.util.*;

public class ClusterManagementPluginView extends PluginView {

    private EventBus eventBus;
    private FlexTable nodesTable;
    private ClusterManagementPluginData clusterManagerPluginData;
    private Map<String, Map<String, Widget>> widgetMap = new HashMap<>();

    /**
     * Основной конструктор
     *
     * @param plugin плагин, являющийся по сути, контроллером (или представителем) в паттерне MVC
     */
    protected ClusterManagementPluginView(Plugin plugin, EventBus eventBus) {
        super(plugin);
        this.eventBus = eventBus;

    }

    @Override
    public IsWidget getViewWidget() {
        clusterManagerPluginData = plugin.getInitialData();
        return buildRootPanel();
    }

    private Widget buildRefreshButton() {
        ConfiguredButton refreshButton = GlobalCacheControlUtils.createButton(GlobalCacheControlUtils.STAT_REFRESH, GlobalCacheControlUtils.BTN_IMG_REFRESH);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshStatisticsModel();
            }
        });
        return refreshButton;
    }


    private void refreshStatisticsModel() {
        Command command = new Command("refreshStatistics", "cluster.management.plugin", clusterManagerPluginData);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining statistics");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                clusterManagerPluginData = (ClusterManagementPluginData) result;
                fillNodesTable(clusterManagerPluginData.getAllNodes());
                refreshMap(clusterManagerPluginData.getCurrentNodeInfo(), "currentNode");
                refreshMap(clusterManagerPluginData.getManagerNodeInfo(), "managementNode");
                Window.alert("Статистика обновленна.");
            }
        });
    }

    private void refreshMap(ClusterNodeInfoViewModel currentNodeInfo, String nodeKey) {
        Map<String, Widget> rowMap = widgetMap.get(nodeKey);
        ((Label)rowMap.get("nodeId")).setText(currentNodeInfo.getNodeId());
        ((Label)rowMap.get("serverName")).setText(currentNodeInfo.getNodeName());
        ((Label)rowMap.get("lastUpdate")).setText(currentNodeInfo.getLastAvailableDateString());
    }


    private Widget buildNodeInfoBox(ClusterNodeInfoViewModel info, String boxName, String key){

        Map<String, Widget> rowMap =  new HashMap<String, Widget>();

        Panel rootPanel = new VerticalPanel();
        rootPanel.setTitle(boxName);
        rootPanel.setStyleName("nodeInfoBox");

        Label nodeBoxWidget = new Label(boxName);
        rootPanel.add(nodeBoxWidget);
        Panel rowUID  = new HorizontalPanel();

        rowUID.add(new Label("UNID сервера"));
        Label nodeId = new Label(info.getNodeId());
        rowMap.put("nodeId", nodeId);
        rowUID.add(nodeId);
        rootPanel.add(rowUID);

        Panel rowName  = new HorizontalPanel();
        rowName.add(new Label("Имя сервера"));
        Label serverName = new Label(info.getNodeName());
        rowMap.put("serverName", serverName);
        rowName.add(serverName);
        rootPanel.add(rowName);

        Panel rowAvailable  = new HorizontalPanel();
        rowAvailable.add(new Label("Дата обновления"));
        Label lastUpdateLabel = new Label(info.getLastAvailableDateString());
        rowAvailable.add(lastUpdateLabel);
        rootPanel.add(rowAvailable);

        rowMap.put("lastUpdate", lastUpdateLabel);
        widgetMap.put(key, rowMap);
        return rootPanel;
    }

    private Widget buildRootPanel() {
        Panel rootPanel = new AbsolutePanel();

        Panel actionPanel = new HorizontalPanel();
        actionPanel.add(buildRefreshButton());
        rootPanel.add(actionPanel);

        rootPanel.add(buildNodeInfoBox(clusterManagerPluginData.getCurrentNodeInfo(), "Текущий Сервер", "currentNode"));
        rootPanel.add(buildNodeInfoBox(clusterManagerPluginData.getCurrentNodeInfo(), "Менеджер Сервер","managementNode"));
        nodesTable = new FlexTable();
        nodesTable.setStyleName("nodeTable");
        fillNodesTable(clusterManagerPluginData.getAllNodes());
        rootPanel.add(nodesTable);

        return rootPanel;
    }

    private void fillNodesTable(List<ClusterNodeInfoViewModel> allNodes) {

        nodesTable.removeAllRows();

        // build header
        nodesTable.setWidget(0, 1, new InlineHTML("<span>UIND</span>"));
        nodesTable.setWidget(0, 2, new InlineHTML("<span>Имя</span>"));
        nodesTable.setWidget(0, 3, new InlineHTML("<span>Обновление</span>"));
        nodesTable.setWidget(0, 4, new InlineHTML("<span>Активные Роли</span>"));
        nodesTable.setWidget(0, 5, new InlineHTML("<span>Доступные Роли</span>"));

        int index = 1;
        for(ClusterNodeInfoViewModel node : allNodes){
            nodesTable.setWidget(index, 1, new Label(node.getNodeId()));
            nodesTable.setWidget(index, 2, new Label(node.getNodeName()));
            nodesTable.setWidget(index, 3, new Label(node.getLastAvailableDateString()));
            nodesTable.setWidget(index, 4, new Label(listToString(node.getActiveRoles())));
            nodesTable.setWidget(index, 5, new Label(listToString(node.getAvailableRoles())));
            index++;
        }
    }

    private String listToString(Set<String> items) {
        StringBuilder builder = new StringBuilder();
        for(String data : items){
            if(builder.length() > 0){
                builder.append(", ");
             }
             builder.append(data);
        }
        return builder.toString();
    }

}
