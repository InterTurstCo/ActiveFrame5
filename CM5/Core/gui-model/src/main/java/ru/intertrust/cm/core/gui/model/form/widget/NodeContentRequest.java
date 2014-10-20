package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;

import java.util.ArrayList;
import java.util.Map;

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
    private FormattingConfig formattingConfig;
    private Id rootId;
    private Map<String, PopupTitlesHolder> titlesHolderMap;
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

    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
    }

    public Id getRootId() {
        return rootId;
    }

    public void setRootId(Id rootId) {
        this.rootId = rootId;
    }

    public Map<String, PopupTitlesHolder> getTitlesHolderMap() {
        return titlesHolderMap;
    }

    public void setTitlesHolderMap(Map<String, PopupTitlesHolder> titlesHolderMap) {
        this.titlesHolderMap = titlesHolderMap;
    }
}
