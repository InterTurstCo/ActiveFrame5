package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.LinkedHashSet;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 24.10.13
 * Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class SuggestionRequest implements Dto {

    private String text;
    private String collectionName;
    private String dropdownPattern;
    private String selectionPattern;
    private String inputTextFilterName;
    private String idsExclusionFilterName;
    private LinkedHashSet<Id> excludeIds = new LinkedHashSet<Id>();

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

    public String getDropdownPattern() {
        return dropdownPattern;
    }

    public void setDropdownPattern(String pattern) {
        this.dropdownPattern = pattern;
    }

    public LinkedHashSet<Id> getExcludeIds() {
        return excludeIds;
    }

    public void setExcludeIds(LinkedHashSet<Id> excludeIds) {
        this.excludeIds = excludeIds;
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
}
