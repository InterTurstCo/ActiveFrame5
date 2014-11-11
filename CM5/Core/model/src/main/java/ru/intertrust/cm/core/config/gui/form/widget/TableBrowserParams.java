package ru.intertrust.cm.core.config.gui.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;
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
    private Collection<Id> excludedIds;
    private boolean displayChosenValues;
    private boolean singleChoice;
    private int pageSize;
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;
    public TableBrowserParams() {
    }

    public Collection<Id> getExcludedIds() {
        return excludedIds == null ? new HashSet<Id>() : excludedIds;
    }

    public TableBrowserParams setExcludedIds(Collection<Id> excludedIds) {
        this.excludedIds = excludedIds;
        return this;
    }

    public boolean isDisplayChosenValues() {
        return displayChosenValues;
    }

    public TableBrowserParams setDisplayChosenValues(boolean displayChosenValues) {
        this.displayChosenValues = displayChosenValues;
        return this;
    }

    public boolean isSingleChoice() {
        return singleChoice;
    }

    public TableBrowserParams setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
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
}
