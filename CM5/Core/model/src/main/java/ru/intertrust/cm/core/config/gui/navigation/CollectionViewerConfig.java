package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.config.gui.form.widget.SearchAreaRefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.SelectionFiltersConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
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

    @Element(name = "child-collection", required = false)
    private ChildCollectionConfig childCollectionConfig;

    @Element(name = "rows-selection", required = false)
    private RowsSelectionConfig rowsSelectionConfig;

    @Element(name = "collection-extra-filters",required = false)
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;

    @Element(name = "user-settings",required = false)
    private UserSettingsConfig userSettingsConfig;

    @Element(name = "current-row-change",required = false)
    private CurrentRowChangeConfig currentRowChangeConfig;

    private TableBrowserParams tableBrowserParams;

    private boolean isEmbedded;

    //Used for hierarchical collections. Not intended to be used in xml configs.
    //Marked as @Attribute to be able to serialize the value in DB (in bu_nav_link_collection.collection_viewer)
    @Attribute(name="hierarchical", required = false)
    private boolean isHierarchical;

    @Attribute(name="style-name", required = false)
    private String styleName;

    @Attribute(name="insert-top", required = false)
    private Boolean insertTop = false;

    //Used for hierarchical collections. Not intended to be used in xml configs.
    //Marked as @Element to be able to serialize the value in DB (in bu_nav_link_collection.collection_viewer)
    @Element(name = "hierarchical-filters", required = false)
    private CollectionExtraFiltersConfig hierarchicalFiltersConfig;

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public Boolean getInsertTop() {
        return insertTop;
    }

    public void setInsertTop(Boolean insertTop) {
        this.insertTop = insertTop;
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

    public CollectionExtraFiltersConfig getCollectionExtraFiltersConfig() {
        return collectionExtraFiltersConfig;
    }

    public void setCollectionExtraFiltersConfig(CollectionExtraFiltersConfig collectionExtraFiltersConfig) {
        this.collectionExtraFiltersConfig = collectionExtraFiltersConfig;
    }

    public UserSettingsConfig getUserSettingsConfig() {
        return userSettingsConfig;
    }

    public void setUserSettingsConfig(UserSettingsConfig userSettingsConfig) {
        this.userSettingsConfig = userSettingsConfig;
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

    public CurrentRowChangeConfig getCurrentRowChangeConfig() {
        return currentRowChangeConfig;
    }

    public void setCurrentRowChangeConfig(CurrentRowChangeConfig currentRowChangeConfig) {
        this.currentRowChangeConfig = currentRowChangeConfig;
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
        return tableBrowserParams != null && tableBrowserParams.isDisplayChosenValues();
    }

    public boolean isSingleChoice() {
        return tableBrowserParams == null || tableBrowserParams.isDisplayCheckBoxes();
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

    public CollectionExtraFiltersConfig getHierarchicalFiltersConfig() {
        return hierarchicalFiltersConfig;
    }

    public void setHierarchicalFiltersConfig(CollectionExtraFiltersConfig hierarchicalFiltersConfig) {
        this.hierarchicalFiltersConfig = hierarchicalFiltersConfig;
    }

    public boolean isEmbedded() {
        return isEmbedded;
    }

    public void setEmbedded(boolean embedded) {
        this.isEmbedded = embedded;
    }

    public RowsSelectionConfig getRowsSelectionConfig() {
        return rowsSelectionConfig;
    }

    public void setRowsSelectionConfig(RowsSelectionConfig rowsSelectionConfig) {
        this.rowsSelectionConfig = rowsSelectionConfig;
    }

    public ChildCollectionConfig getChildCollectionConfig() {
        return childCollectionConfig;
    }

    public void setChildCollectionConfig(ChildCollectionConfig childCollectionConfig) {
        this.childCollectionConfig = childCollectionConfig;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectionViewerConfig that = (CollectionViewerConfig) o;

        if (isEmbedded != that.isEmbedded) return false;
        if (isHierarchical != that.isHierarchical) return false;
        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.collectionRefConfig) : that.collectionRefConfig != null)
            return false;
        if (collectionViewRefConfig != null ? !collectionViewRefConfig.equals(that.collectionViewRefConfig) : that.collectionViewRefConfig != null)
            return false;
        if (searchAreaRefConfig != null ? !searchAreaRefConfig.equals(that.searchAreaRefConfig) : that.searchAreaRefConfig != null)
            return false;
        if (searchCollectionRefConfig != null ? !searchCollectionRefConfig.equals(that.searchCollectionRefConfig) : that.searchCollectionRefConfig != null)
            return false;
        if (defaultSortCriteriaConfig != null ? !defaultSortCriteriaConfig.equals(that.defaultSortCriteriaConfig) : that.defaultSortCriteriaConfig != null)
            return false;
        if (initialFiltersConfig != null ? !initialFiltersConfig.equals(that.initialFiltersConfig) : that.initialFiltersConfig != null)
            return false;
        if (selectionFiltersConfig != null ? !selectionFiltersConfig.equals(that.selectionFiltersConfig) : that.selectionFiltersConfig != null)
            return false;
        if (filterPanelConfig != null ? !filterPanelConfig.equals(that.filterPanelConfig) : that.filterPanelConfig != null)
            return false;
        if (toolBarConfig != null ? !toolBarConfig.equals(that.toolBarConfig) : that.toolBarConfig != null)
            return false;
        if (nonReadElementsDefinitionConfig != null ? !nonReadElementsDefinitionConfig.equals(that.nonReadElementsDefinitionConfig) : that.nonReadElementsDefinitionConfig != null)
            return false;
        if (childCollectionConfig != null ? !childCollectionConfig.equals(that.childCollectionConfig) : that.childCollectionConfig != null)
            return false;
        if (rowsSelectionConfig != null ? !rowsSelectionConfig.equals(that.rowsSelectionConfig) : that.rowsSelectionConfig != null)
            return false;
        if (collectionExtraFiltersConfig != null ? !collectionExtraFiltersConfig.equals(that.collectionExtraFiltersConfig) : that.collectionExtraFiltersConfig != null)
            return false;
        if (userSettingsConfig != null ? !userSettingsConfig.equals(that.userSettingsConfig) : that.userSettingsConfig != null)
            return false;
        if (currentRowChangeConfig != null ? !currentRowChangeConfig.equals(that.currentRowChangeConfig) : that.currentRowChangeConfig != null)
            return false;
        if (tableBrowserParams != null ? !tableBrowserParams.equals(that.tableBrowserParams) : that.tableBrowserParams != null)
            return false;
        if (hierarchicalFiltersConfig != null ? !hierarchicalFiltersConfig.equals(that.hierarchicalFiltersConfig) : that.hierarchicalFiltersConfig != null)
            return false;
        if (styleName != null ? !styleName.equals(that.styleName) : that.styleName != null)
            return false;
        if (insertTop != null ? !insertTop.equals(that.insertTop) : that.insertTop != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return collectionRefConfig != null ? collectionRefConfig.hashCode() : 0;
    }

    @Override
    public String getComponentName() {
        return "collection.plugin";
    }
}

