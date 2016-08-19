package ru.intertrust.cm.core.gui.model.plugin.hierarchy;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Элемент хранения состояния узла для истории виджета.
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

    public HierarchyHistoryNode(){}

    public HierarchyHistoryNode(String anId,String  aParentId){
        nodeId = anId;
        parentId = aParentId;
        children = new ArrayList<HierarchyHistoryNode>();
    }


    public Collection<HierarchyHistoryNode> getChildren() {
        return children;
    }

    public void add(HierarchyHistoryNode newNode){
        if(nodeId.equals(newNode.getParentId())){
            children.add(newNode);
        } else {
            findParent(newNode.getParentId()).add(newNode);
        }
    }

    private HierarchyHistoryNode findParent(String id){
      for(HierarchyHistoryNode n : children){
          if(n.getNodeId().equals(id))
              return n;
          else
              return findParent(n.getNodeId());
      }
        return null;
    }

    public void remove(HierarchyHistoryNode newNode){
        if(nodeId.equals(newNode.getNodeId()) && newNode.getParentId() == null){
            children.clear();
        } else {
            removeFromChildren(children,newNode);
        }

    }

    private void removeFromChildren(Collection children, HierarchyHistoryNode newNode){
        for (Iterator<HierarchyHistoryNode> iterator = children.iterator(); iterator.hasNext();) {
            HierarchyHistoryNode N = iterator.next();
            if (N.getNodeId().equals(newNode.getNodeId()) && N.getParentId().equals(newNode.getParentId())) {
                iterator.remove();
                return;
            } else {
                removeFromChildren(N.children,newNode);
            }
        }

    }

    @Override
    public boolean equals(Object otherObject){
        if (this == otherObject) return true;
        if (otherObject == null) return false;
        if (getClass() != otherObject.getClass()) return false;
        HierarchyHistoryNode other = (HierarchyHistoryNode)otherObject;

        return Objects.equals(nodeId,other.nodeId) && Objects.equals(parentId,other.parentId);
    }

    @Override
    public int hashCode(){
        return Objects.hash(nodeId,parentId);
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setChildren(Collection<HierarchyHistoryNode> children) {
        this.children = children;
    }
}
