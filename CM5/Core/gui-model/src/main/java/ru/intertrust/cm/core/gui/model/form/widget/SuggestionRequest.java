package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;
import ru.intertrust.cm.core.gui.model.filters.ComplexFiltersParams;

import java.util.LinkedHashSet;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 24.10.13
 * Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class SuggestionRequest implements Dto {

    private String collectionName;
    private String dropdownPattern;
    private String selectionPattern;
    private String idsExclusionFilterName;
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;
    private LinkedHashSet<Id> excludeIds = new LinkedHashSet<Id>();
    private FormattingConfig formattingConfig;
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;
    private boolean tooltipContent;
    private LazyLoadState lazyLoadState;
    private ComplexFiltersParams complexFiltersParams;

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

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }

    public String getIdsExclusionFilterName() {
        return idsExclusionFilterName;
    }

    public void setIdsExclusionFilterName(String idsExclusionFilterName) {
        this.idsExclusionFilterName = idsExclusionFilterName;
    }

    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
    }

    public CollectionExtraFiltersConfig getCollectionExtraFiltersConfig() {
        return collectionExtraFiltersConfig;
    }

    public void setCollectionExtraFiltersConfig(CollectionExtraFiltersConfig collectionExtraFiltersConfig) {
        this.collectionExtraFiltersConfig = collectionExtraFiltersConfig;
    }

    public boolean isTooltipContent() {
        return tooltipContent;
    }

    public void setTooltipContent(boolean tooltipContent) {
        this.tooltipContent = tooltipContent;
    }

    public LazyLoadState getLazyLoadState() {
        return lazyLoadState;
    }

    public void setLazyLoadState(LazyLoadState lazyLoadState) {
        this.lazyLoadState = lazyLoadState;
    }

    public ComplexFiltersParams getComplexFiltersParams() {
        return complexFiltersParams;
    }

    public void setComplexFiltersParams(ComplexFiltersParams complexFiltersParams) {
        this.complexFiltersParams = complexFiltersParams;
    }
}
