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

    @Element(name = "sort-criteria", required = false)
    private SortCriteriaConfig sortCriteriaConfig;

    private List<Id> excludedIds = new ArrayList<Id>();

    private boolean displayChosenValues = true;

    private boolean singleChoice = true;

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public SortCriteriaConfig getSortCriteriaConfig() {
        return sortCriteriaConfig;
    }

    public void setSortCriteriaConfig(SortCriteriaConfig sortCriteriaConfig) {
        this.sortCriteriaConfig = sortCriteriaConfig;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionViewerConfig that = (CollectionViewerConfig) o;

        if (sortCriteriaConfig != null ? !sortCriteriaConfig.equals(that.sortCriteriaConfig) : that.
                sortCriteriaConfig != null) {

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

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionRefConfig != null ? collectionRefConfig.hashCode() : 0;
        result = 31 * result + (collectionViewRefConfig != null ? collectionViewRefConfig.hashCode() : 0);
        result = 31 * result + (sortCriteriaConfig != null ? sortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (searchAreaRefConfig != null ? searchAreaRefConfig.hashCode() : 0);
        result = 31 * result + (searchCollectionRefConfig != null ? searchCollectionRefConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "collection.plugin";
    }
}

