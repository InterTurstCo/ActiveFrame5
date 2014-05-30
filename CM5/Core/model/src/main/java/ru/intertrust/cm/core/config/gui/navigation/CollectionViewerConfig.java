package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SearchAreaRefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SearchCollectionRefConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 20/9/13
 *         Time: 12:05 PM
 */
@Root(name = "collection-viewer")
public class CollectionViewerConfig extends PluginConfig{
    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "collection-view-ref", required = false)
    private CollectionViewRefConfig collectionViewRefConfig;

    @Element(name = "search-area-ref", required = false)
    private SearchAreaRefConfig searchAreaRefConfig;

    @Element(name = "search-collection-ref", required = false)
    private SearchCollectionRefConfig searchCollectionRefConfig;

    @Element(name = "default-sort-criteria", required = false)
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;

    private List<Id> excludedIds = new ArrayList<Id>();

    @Element(name = "initial-filters", required = false)
    private InitialFiltersConfig initialFiltersConfig;

    @Element(name = "filter-panel", required = false)
    private FilterPanelConfig filterPanelConfig;

    private boolean displayChosenValues = true;

    private boolean singleChoice = true;
    private String filterName;
    private String filterValue;

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }

    public CollectionViewRefConfig getCollectionViewRefConfig() {
        return collectionViewRefConfig;
    }

    public void setCollectionViewRefConfig(CollectionViewRefConfig collectionViewRefConfig) {
        this.collectionViewRefConfig = collectionViewRefConfig;
    }

    public SearchAreaRefConfig getSearchAreaRefConfig() {
        return searchAreaRefConfig;
    }

    public void setSearchAreaRefConfig(SearchAreaRefConfig searchAreaRefConfig) {
        this.searchAreaRefConfig = searchAreaRefConfig;
    }

    public List<Id> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<Id> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public boolean isDisplayChosenValues() {
        return displayChosenValues;
    }

    public void setDisplayChosenValues(boolean displayChosenValues) {
        this.displayChosenValues = displayChosenValues;
    }

    public boolean isSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    public SearchCollectionRefConfig getSearchCollectionRefConfig() {
        return searchCollectionRefConfig;
    }

    public void setSearchCollectionRefConfig(SearchCollectionRefConfig searchCollectionRefConfig) {
        this.searchCollectionRefConfig = searchCollectionRefConfig;
    }

    public InitialFiltersConfig getInitialFiltersConfig() {
        return initialFiltersConfig;
    }

    public void setInitialFiltersConfig(InitialFiltersConfig initialFiltersConfig) {
        this.initialFiltersConfig = initialFiltersConfig;
    }

    public FilterPanelConfig getFilterPanelConfig() {
        return filterPanelConfig;
    }

    public void setFilterPanelConfig(FilterPanelConfig filterPanelConfig) {
        this.filterPanelConfig = filterPanelConfig;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionViewerConfig that = (CollectionViewerConfig) o;

        if (defaultSortCriteriaConfig != null ? !defaultSortCriteriaConfig.equals(that.defaultSortCriteriaConfig) : that.
                defaultSortCriteriaConfig != null) {

            return false;
        }

        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.getCollectionRefConfig()) : that.
                getCollectionRefConfig() != null) {
            return false;
        }

        if (collectionViewRefConfig != null ? !collectionViewRefConfig.equals(that.getCollectionViewRefConfig()) : that.
                getCollectionViewRefConfig() != null) {
            return false;
        }
        if (searchAreaRefConfig != null ? !searchAreaRefConfig.equals(that.searchAreaRefConfig) : that.
                searchAreaRefConfig != null) {
            return false;
        }
        if (searchCollectionRefConfig != null ? !searchCollectionRefConfig.equals(that.searchCollectionRefConfig) : that.
                searchCollectionRefConfig != null) {
            return false;
        }
        if (initialFiltersConfig != null ? !initialFiltersConfig.equals(that.initialFiltersConfig) : that.initialFiltersConfig != null) {
            return false;
        }
        if (filterPanelConfig != null ? !filterPanelConfig.equals(that.filterPanelConfig) : that.filterPanelConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionRefConfig != null ? collectionRefConfig.hashCode() : 0;
        result = 31 * result + (collectionViewRefConfig != null ? collectionViewRefConfig.hashCode() : 0);
        result = 31 * result + (defaultSortCriteriaConfig != null ? defaultSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (searchAreaRefConfig != null ? searchAreaRefConfig.hashCode() : 0);
        result = 31 * result + (searchCollectionRefConfig != null ? searchCollectionRefConfig.hashCode() : 0);
        result = 31 * result + (initialFiltersConfig != null ? initialFiltersConfig.hashCode() : 0);
        result = 31 * result + (filterPanelConfig != null ? filterPanelConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "collection.plugin";
    }
}

