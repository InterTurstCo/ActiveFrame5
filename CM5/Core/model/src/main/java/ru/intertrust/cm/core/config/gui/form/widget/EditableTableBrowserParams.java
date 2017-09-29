package ru.intertrust.cm.core.config.gui.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Ravil on 28.09.2017.
 */
public class EditableTableBrowserParams extends TableBrowserParams implements Dto {
    private Dto complexFiltersParams;
    private Collection<Id> ids;
    private boolean displayOnlySelectedIds;
    private boolean displayChosenValues;
    private boolean displayCheckBoxes;
    private Integer pageSize;
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;
    private SelectionFiltersConfig selectionFiltersConfig;
    private boolean hasColumnButtons;
    public EditableTableBrowserParams() {
    }

    public Collection<Id> getIds() {
        return ids == null ? new HashSet<Id>() : ids;
    }

    public EditableTableBrowserParams setIds(Collection<Id> ids) {
        this.ids = ids;
        return this;
    }

    public boolean isDisplayChosenValues() {
        return displayChosenValues;
    }

    public EditableTableBrowserParams setDisplayChosenValues(boolean displayChosenValues) {
        this.displayChosenValues = displayChosenValues;
        return this;
    }

    public boolean isDisplayCheckBoxes() {
        return displayCheckBoxes;
    }

    public EditableTableBrowserParams setDisplayCheckBoxes(boolean displayCheckBoxes) {
        this.displayCheckBoxes = displayCheckBoxes;
        return this;
    }

    public int getPageSize() {
        return pageSize == null ? ModelConstants.INIT_ROWS_NUMBER : pageSize;
    }

    public EditableTableBrowserParams  setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Dto getComplexFiltersParams() {
        return complexFiltersParams;
    }

    public EditableTableBrowserParams setComplexFiltersParams(Dto complexFiltersParams) {
        this.complexFiltersParams = complexFiltersParams;
        return this;
    }

    public CollectionExtraFiltersConfig getCollectionExtraFiltersConfig() {
        return collectionExtraFiltersConfig;
    }

    public EditableTableBrowserParams setCollectionExtraFiltersConfig(CollectionExtraFiltersConfig collectionExtraFiltersConfig) {
        this.collectionExtraFiltersConfig = collectionExtraFiltersConfig;
        return this;
    }
    public EditableTableBrowserParams setSelectionFiltersConfig(SelectionFiltersConfig selectionFiltersConfig) {
        this.selectionFiltersConfig = selectionFiltersConfig;
        return this;
    }

    public SelectionFiltersConfig getSelectionFiltersConfig() {
        return selectionFiltersConfig;
    }

    public boolean isDisplayOnlySelectedIds() {
        return displayOnlySelectedIds;
    }

    public EditableTableBrowserParams setDisplayOnlySelectedIds(boolean displayOnlySelectedIds) {
        this.displayOnlySelectedIds = displayOnlySelectedIds;
        return this;
    }

    public boolean hasColumnButtons() {
        return hasColumnButtons;
    }

    public EditableTableBrowserParams setHasColumnButtons(boolean hasColumnButtons) {
        this.hasColumnButtons = hasColumnButtons;
        return this;
    }
}
