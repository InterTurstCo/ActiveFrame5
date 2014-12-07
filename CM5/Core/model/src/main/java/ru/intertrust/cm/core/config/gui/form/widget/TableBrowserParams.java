package ru.intertrust.cm.core.config.gui.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 25.08.2014
 *         Time: 20:43
 */
public class TableBrowserParams implements Dto {
    private Dto complicatedFiltersParams;
    private Collection<Id> ids;
    private boolean displayOnlySelectedIds;
    private boolean displayChosenValues;
    private boolean displayCheckBoxes;
    private int pageSize;
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;
    private SelectionFiltersConfig selectionFiltersConfig;
    public TableBrowserParams() {
    }

    public Collection<Id> getIds() {
        return ids == null ? new HashSet<Id>() : ids;
    }

    public TableBrowserParams setIds(Collection<Id> ids) {
        this.ids = ids;
        return this;
    }

    public boolean isDisplayChosenValues() {
        return displayChosenValues;
    }

    public TableBrowserParams setDisplayChosenValues(boolean displayChosenValues) {
        this.displayChosenValues = displayChosenValues;
        return this;
    }

    public boolean isDisplayCheckBoxes() {
        return displayCheckBoxes;
    }

    public TableBrowserParams setDisplayCheckBoxes(boolean displayCheckBoxes) {
        this.displayCheckBoxes = displayCheckBoxes;
        return this;
    }

    public int getPageSize() {
        return pageSize == 0 ? ModelConstants.INIT_ROWS_NUMBER : pageSize;
    }

    public TableBrowserParams  setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Dto getComplicatedFiltersParams() {
        return complicatedFiltersParams;
    }

    public TableBrowserParams setComplicatedFiltersParams(Dto complicatedFiltersParams) {
        this.complicatedFiltersParams = complicatedFiltersParams;
        return this;
    }

    public CollectionExtraFiltersConfig getCollectionExtraFiltersConfig() {
        return collectionExtraFiltersConfig;
    }

    public TableBrowserParams setCollectionExtraFiltersConfig(CollectionExtraFiltersConfig collectionExtraFiltersConfig) {
        this.collectionExtraFiltersConfig = collectionExtraFiltersConfig;
        return this;
    }
    public TableBrowserParams setSelectionFiltersConfig(SelectionFiltersConfig selectionFiltersConfig) {
        this.selectionFiltersConfig = selectionFiltersConfig;
        return this;
    }

    public SelectionFiltersConfig getSelectionFiltersConfig() {
        return selectionFiltersConfig;
    }

    public boolean isDisplayOnlySelectedIds() {
        return displayOnlySelectedIds;
    }

    public TableBrowserParams setDisplayOnlySelectedIds(boolean displayOnlySelectedIds) {
        this.displayOnlySelectedIds = displayOnlySelectedIds;
        return this;
    }
}
