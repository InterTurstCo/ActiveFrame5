package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SearchAreaRefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SearchCollectionRefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserParams;
import ru.intertrust.cm.core.config.gui.form.widget.filter.AbstractFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.counters.NonReadElementsDefinitionConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 20/9/13
 *         Time: 12:05 PM
 */
@Root(name = "collection-viewer")
public class CollectionViewerConfig extends PluginConfig {
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

    @Element(name = "initial-filters", required = false)
    private InitialFiltersConfig initialFiltersConfig;

    private SelectionFiltersConfig selectionFiltersConfig;

    @Element(name = "filter-panel", required = false)
    private FilterPanelConfig filterPanelConfig;

    @Element(name = "tool-bar", required = false)
    private ToolBarConfig toolBarConfig;

    @Element(name = "non-read-elements-definition", required = false)
    private NonReadElementsDefinitionConfig nonReadElementsDefinitionConfig;

    private TableBrowserParams tableBrowserParams;

    private boolean isHierarchical;

    @Element(name = "hierarchical-filters", required = false)
    private AbstractFiltersConfig hierarchicalFiltersConfig;

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        if (defaultSortCriteriaConfig == null) {
            defaultSortCriteriaConfig = new DefaultSortCriteriaConfig();
        }
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

    public SelectionFiltersConfig getSelectionFiltersConfig() {
        return selectionFiltersConfig;
    }

    public void setSelectionFiltersConfig(SelectionFiltersConfig selectionFiltersConfig) {
        this.selectionFiltersConfig = selectionFiltersConfig;
    }

    public void setHierarchical(boolean isHierarchical) {
        this.isHierarchical = isHierarchical;
    }

    public boolean isDisplayChosenValues() {
        if (isHierarchical) {
            return true;
        }
        return tableBrowserParams == null ? false : tableBrowserParams.isDisplayChosenValues();
    }

    public boolean isSingleChoice() {
        return tableBrowserParams == null ? true : tableBrowserParams.isSingleChoice();
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

    public ToolBarConfig getToolBarConfig() {
        return toolBarConfig;
    }

    public NonReadElementsDefinitionConfig getNonReadElementsDefinitionConfig() {
        return nonReadElementsDefinitionConfig;
    }

    public void setNonReadElementsDefinitionConfig(NonReadElementsDefinitionConfig nonReadElementsDefinitionConfig) {
        this.nonReadElementsDefinitionConfig = nonReadElementsDefinitionConfig;
    }

    public TableBrowserParams getTableBrowserParams() {
        return tableBrowserParams;
    }

    public void setTableBrowserParams(TableBrowserParams tableBrowserParams) {
        this.tableBrowserParams = tableBrowserParams;
    }
    public int getRowsChunk(){
        return tableBrowserParams == null ? ModelConstants.INIT_ROWS_NUMBER : tableBrowserParams.getPageSize();
    }

    public AbstractFiltersConfig getHierarchicalFiltersConfig() {
        return hierarchicalFiltersConfig;
    }

    public void setHierarchicalFiltersConfig(AbstractFiltersConfig hierarchicalFiltersConfig) {
        this.hierarchicalFiltersConfig = hierarchicalFiltersConfig;
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

        if (nonReadElementsDefinitionConfig != null ? !nonReadElementsDefinitionConfig.equals(that.nonReadElementsDefinitionConfig)
                : that.nonReadElementsDefinitionConfig!= null) {
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

