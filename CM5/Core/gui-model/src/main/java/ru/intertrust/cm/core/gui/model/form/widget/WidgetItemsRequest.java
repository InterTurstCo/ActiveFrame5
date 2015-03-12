package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.SelectionSortCriteriaConfig;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.12.13
 *         Time: 13:15
 */
public class WidgetItemsRequest implements Dto {
    private String selectionPattern;
    private ArrayList<Id> selectedIds;
    private String collectionName;
    private SelectionSortCriteriaConfig selectionSortCriteriaConfig;
    private FormattingConfig formattingConfig;
    private SelectionFiltersConfig selectionFiltersConfig;
    public String getSelectionPattern() {
        return selectionPattern;
    }
    private ComplexFiltersParams complexFiltersParams;

    public void setSelectionPattern(String selectionPattern) {
        this.selectionPattern = selectionPattern;
    }

    public ArrayList<Id> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(ArrayList<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
    }

    public SelectionFiltersConfig getSelectionFiltersConfig() {
        return selectionFiltersConfig;
    }

    public void setSelectionFiltersConfig(SelectionFiltersConfig selectionFiltersConfig) {
        this.selectionFiltersConfig = selectionFiltersConfig;
    }

    public SelectionSortCriteriaConfig getSelectionSortCriteriaConfig() {
        return selectionSortCriteriaConfig;
    }

    public void setSelectionSortCriteriaConfig(SelectionSortCriteriaConfig selectionSortCriteriaConfig) {
        this.selectionSortCriteriaConfig = selectionSortCriteriaConfig;
    }

    public ComplexFiltersParams getComplexFiltersParams() {
        return complexFiltersParams;
    }

    public void setComplexFiltersParams(ComplexFiltersParams complexFiltersParams) {
        this.complexFiltersParams = complexFiltersParams;
    }
}
