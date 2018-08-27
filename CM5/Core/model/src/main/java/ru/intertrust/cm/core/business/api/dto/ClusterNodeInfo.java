package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;
import java.util.Set;

/**
 * Created by Vitaliy.Orlov on 24.08.2018.
 */
public class ClusterNodeInfo implements Dto {
    private String nodeId;
    private String nodeName;
    private Date lastAvailable;
    private Set<String> availableRoles;
    private Set<String> activeRoles;

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
}
