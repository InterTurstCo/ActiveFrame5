package ru.intertrust.cm.core.gui.model.plugin.cluster;


import ru.intertrust.cm.core.gui.model.plugin.PluginData;

import java.util.List;

public class ClusterManagementPluginData extends PluginData {

    private ClusterNodeInfoViewModel currentNodeInfo;
    private ClusterNodeInfoViewModel managerNodeInfo;
    private List<ClusterNodeInfoViewModel> allNodes;

    public ClusterNodeInfoViewModel getCurrentNodeInfo() {
        return currentNodeInfo;
    }

    public void setCurrentNodeInfo(ClusterNodeInfoViewModel currentNodeInfo) {
        this.currentNodeInfo = currentNodeInfo;
    }

    public ClusterNodeInfoViewModel getManagerNodeInfo() {
        return managerNodeInfo;
    }

    public void setManagerNodeInfo(ClusterNodeInfoViewModel managerNodeInfo) {
        this.managerNodeInfo = managerNodeInfo;
    }

    public List<ClusterNodeInfoViewModel> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(List<ClusterNodeInfoViewModel> allNodes) {
        this.allNodes = allNodes;
    }
}
