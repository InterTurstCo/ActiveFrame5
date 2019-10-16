package ru.intertrust.cm.core.gui.model.plugin.hierarchy;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Элемент хранения состояния узла для истории виджета.
 * Умеет рекурсивно добавлять дочерние узлы по паренту, удалять узлы.
 * При удалении сравнивается не только Id узла но и парент т.к. строка из
 * одной и тойже коллекции может быть в разных группах.
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 18.08.2016
 * Time: 14:45
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyHistoryNode implements Dto {

    private String nodeId;
    private String parentId;
    private Collection<HierarchyHistoryNode> children;
    private Boolean opened = false;

    public HierarchyHistoryNode() {
    }

    public HierarchyHistoryNode(String anId, String aParentId) {
        nodeId = anId;
        parentId = aParentId;
        children = new ArrayList<HierarchyHistoryNode>();
    }

    public void add(HierarchyHistoryNode newNode) {
        if (nodeId.equals(newNode.getParentId())) {
            children.add(newNode);
        } else {
            HierarchyHistoryNode parent = findParent(newNode.getParentId(), getChildren());
            if (parent != null) {
                parent.add(newNode);
            }
        }
    }

    private HierarchyHistoryNode findParent(String id, Collection<HierarchyHistoryNode> aChildren) {
        for (HierarchyHistoryNode n : aChildren) {
            if (n.getNodeId().equals(id)) {
                return n;
            } else {
                if (n.getChildren() != null) {
                    HierarchyHistoryNode pNode = findParent(id, n.getChildren());
                    if (pNode == null) {
                        continue;
                    } else
                        return pNode;
                }
            }
        }
        return null;
    }

    /**
     * @param newNode
     * @return true - если закрыт самый верхний уровень
     */
    public Boolean remove(HierarchyHistoryNode newNode) {
        if (nodeId.equals(newNode.getNodeId()) && newNode.getParentId() == null) {
            children.clear();
            return true;
        } else {
            removeFromChildren(children, newNode);
            return false;
        }
    }

    private void removeFromChildren(Collection children, HierarchyHistoryNode newNode) {
        for (Iterator<HierarchyHistoryNode> iterator = children.iterator(); iterator.hasNext(); ) {
            HierarchyHistoryNode N = iterator.next();
            if (N.getNodeId().equals(newNode.getNodeId()) && N.getParentId().equals(newNode.getParentId())) {
                iterator.remove();
                return;
            } else {
                removeFromChildren(N.children, newNode);
            }
        }

    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (otherObject == null) return false;
        if (getClass() != otherObject.getClass()) return false;
        HierarchyHistoryNode other = (HierarchyHistoryNode) otherObject;

        return Objects.equals(nodeId, other.nodeId) && Objects.equals(parentId, other.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, parentId);
    }

    public String getNodeId() {
        return nodeId;
    }


    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Collection<HierarchyHistoryNode> getChildren() {
        return children;
    }

    public Boolean isOpened() {
        return opened;
    }

    public void setOpened(Boolean opened) {
        this.opened = opened;
    }
}
