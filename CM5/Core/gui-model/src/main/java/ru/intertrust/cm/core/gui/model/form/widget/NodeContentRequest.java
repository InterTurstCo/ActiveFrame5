package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.12.13
 *         Time: 13:15
 */
public class NodeContentRequest implements Dto {
    private NodeMetadata nodeMetadata;
    private String parentFilterName;
    private String selectionPattern;
    private SortCriteriaConfig sortCriteriaConfig;
    private String inputTextFilterName;
    private int numberOfItemsToDisplay;
    private int offset;
    private String inputText;
    private boolean selective = true;
    private ArrayList<Id> chosenIds = new ArrayList<Id>();

    public NodeMetadata getNodeMetadata() {
        return nodeMetadata;
    }

    public void setNodeMetadata(NodeMetadata nodeMetadata) {
        this.nodeMetadata = nodeMetadata;
    }

    public String getParentFilterName() {
        return parentFilterName;
    }

    public void setParentFilterName(String parentFilterName) {
        this.parentFilterName = parentFilterName;
    }

    public String getSelectionPattern() {
        return selectionPattern;
    }

    public void setSelectionPattern(String selectionPattern) {
        this.selectionPattern = selectionPattern;
    }

    public String getInputTextFilterName() {
        return inputTextFilterName;
    }

    public void setInputTextFilterName(String inputTextFilterName) {
        this.inputTextFilterName = inputTextFilterName;
    }

    public int getNumberOfItemsToDisplay() {
        return numberOfItemsToDisplay;
    }

    public void setNumberOfItemsToDisplay(int numberOfItemsToDisplay) {
        this.numberOfItemsToDisplay = numberOfItemsToDisplay;
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

    public SortCriteriaConfig getSortCriteriaConfig() {
        return sortCriteriaConfig;
    }

    public void setSortCriteriaConfig(SortCriteriaConfig sortCriteriaConfig) {
        this.sortCriteriaConfig = sortCriteriaConfig;
    }
}
