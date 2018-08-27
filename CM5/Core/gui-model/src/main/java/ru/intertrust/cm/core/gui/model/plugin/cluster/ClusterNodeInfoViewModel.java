package ru.intertrust.cm.core.gui.model.plugin.cluster;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.Date;
import java.util.Set;

/**
 * Created by Vitaliy.Orlov on 23.08.2018.
 */
public class ClusterNodeInfoViewModel implements Dto {

    private String nodeId;
    private String nodeName;
    private Date lastAvailable;
    private String lastAvailableDateString;
    private Set<String> availableRoles;
    private Set<String> activeRoles;

    public ClusterNodeInfoViewModel() {
    }

    public ClusterNodeInfoViewModel(String nodeId, String nodeName, Date lastAvailable, String lastAvailableDateString, Set<String> availableRoles, Set<String> activeRoles) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.lastAvailable = lastAvailable;
        this.availableRoles = availableRoles;
        this.activeRoles = activeRoles;
        this.lastAvailableDateString = lastAvailableDateString;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Date getLastAvailable() {
        return lastAvailable;
    }

    public void setLastAvailable(Date lastAvailable) {
        this.lastAvailable = lastAvailable;
    }

    public Set<String> getAvailableRoles() {
        return availableRoles;
    }

    public void setAvailableRoles(Set<String> availableRoles) {
        this.availableRoles = availableRoles;
    }

    public Set<String> getActiveRoles() {
        return activeRoles;
    }

    public void setActiveRoles(Set<String> activeRoles) {
        this.activeRoles = activeRoles;
    }

    public String getLastAvailableDateString() {
        return lastAvailableDateString;
    }

    public void setLastAvailableDateString(String lastAvailableDateString) {
        this.lastAvailableDateString = lastAvailableDateString;
    }
}
