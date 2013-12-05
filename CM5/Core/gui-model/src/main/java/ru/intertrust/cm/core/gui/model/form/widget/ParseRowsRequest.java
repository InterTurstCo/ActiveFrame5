package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.12.13
 *         Time: 13:15
 */
public class ParseRowsRequest implements Dto {
    private String text;
    private String collectionName;
    private String selectionPattern;
    private String inputTextFilterName;
    private String idsExclusionFilterName;
    private LinkedHashMap<String, String> columnFields = new LinkedHashMap<String, String>();
    private ArrayList<Id> excludeIds = new ArrayList<Id>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
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

    public String getIdsExclusionFilterName() {
        return idsExclusionFilterName;
    }

    public void setIdsExclusionFilterName(String idsExclusionFilterName) {
        this.idsExclusionFilterName = idsExclusionFilterName;
    }

    public Set<String> getColumnFields() {
        return columnFields.keySet();
    }

    public void setColumnFields(LinkedHashMap<String, String> columnFields) {
        this.columnFields = columnFields;
    }

    public ArrayList<Id> getExcludeIds() {
        return excludeIds;
    }

    public void setExcludeIds(ArrayList<Id> excludeIds) {
        this.excludeIds = excludeIds;
    }
}
