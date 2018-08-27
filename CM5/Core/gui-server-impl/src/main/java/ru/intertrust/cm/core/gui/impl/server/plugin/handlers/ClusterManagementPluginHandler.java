package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ClusterManager;
import ru.intertrust.cm.core.business.api.dto.ClusterNodeInfo;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.GlobalCachePluginData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.cluster.ClusterManagementPluginData;
import ru.intertrust.cm.core.gui.model.plugin.cluster.ClusterNodeInfoViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ComponentName("cluster.management.plugin")
public class ClusterManagementPluginHandler extends PluginHandler {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss.SSS");

    @Autowired
    private ClusterManager clusterManager;

    public PluginData initialize(Dto config) {
      return refreshStatistics(config);
    }

    private ClusterNodeInfoViewModel convertInfo(ClusterNodeInfo dataInfo) {
        ClusterNodeInfoViewModel view = new ClusterNodeInfoViewModel();
        view.setNodeId(dataInfo.getNodeId());
        view.setActiveRoles(dataInfo.getActiveRoles());
        view.setAvailableRoles(dataInfo.getAvailableRoles());
        view.setLastAvailable(dataInfo.getLastAvailable());
        view.setNodeName(dataInfo.getNodeName());
        view.setLastAvailableDateString( dataInfo.getLastAvailable() != null ? dateFormat.format(dataInfo.getLastAvailable()) : "");
        return view;
    }


    public ClusterManagementPluginData refreshStatistics(Dto request){
        ClusterManagementPluginData clusterManagementPluginData = new ClusterManagementPluginData();

        Map<String, ClusterNodeInfo> nodesInfo = clusterManager.geNodesInfo();
        ClusterNodeInfo currentNodeInfo = nodesInfo.get(clusterManager.getNodeId());
        ClusterNodeInfo managerNodeInfo = clusterManager.getClusterManagerNodeInfo();
        clusterManagementPluginData.setCurrentNodeInfo(convertInfo(currentNodeInfo));
        clusterManagementPluginData.setManagerNodeInfo(convertInfo(managerNodeInfo));
        clusterManagementPluginData.setAllNodes(new ArrayList<ClusterNodeInfoViewModel>());

        for(Map.Entry<String, ClusterNodeInfo> entry : nodesInfo.entrySet()){
            clusterManagementPluginData.getAllNodes().add(convertInfo(entry.getValue()));
        }
        return clusterManagementPluginData;
    }

}
