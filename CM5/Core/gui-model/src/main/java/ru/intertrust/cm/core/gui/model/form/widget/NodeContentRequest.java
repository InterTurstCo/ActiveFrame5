package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.12.13
 *         Time: 13:15
 */
public class NodeContentRequest implements Dto {

    private NodeCollectionDefConfig nodeCollectionDefConfig;
    private int numberOfItemsToDisplay;
    private int offset;
    private String inputText;
    private boolean selective = true;
    private Id parentId;
    private ArrayList<Id> chosenIds = new ArrayList<Id>();
    private boolean openChildren = true;
    public int getNumberOfItemsToDisplay() {
        return numberOfItemsToDisplay;
    }

    public void setNumberOfItemsToDisplay(int numberOfItemsToDisplay) {
        this.numberOfItemsToDisplay = numberOfItemsToDisplay;
    }

    public boolean isOpenChildren() {
        return openChildren;
    }

    public void setOpenChildren(boolean openChildren) {
        this.openChildren = openChildren;
    }

    public ArrayList<Id> getChosenIds() {
        return chosenIds;
    }

    public void setChosenIds(ArrayList<Id> chosenIds) {
        this.chosenIds = chosenIds;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isSelective() {
        return selective;
    }

    public void setSelective(boolean selective) {
        this.selective = selective;
    }

    public NodeCollectionDefConfig getNodeCollectionDefConfig() {
        return nodeCollectionDefConfig;
    }

    public void setNodeCollectionDefConfig(NodeCollectionDefConfig nodeCollectionDefConfig) {
        this.nodeCollectionDefConfig = nodeCollectionDefConfig;
    }

    public Id getParentId() {
        return parentId;
    }

    public void setParentId(Id parentId) {
        this.parentId = parentId;
    }
}
