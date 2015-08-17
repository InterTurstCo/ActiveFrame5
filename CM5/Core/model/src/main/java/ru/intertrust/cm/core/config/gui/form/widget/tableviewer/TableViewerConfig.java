package ru.intertrust.cm.core.config.gui.form.widget.tableviewer;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.NotNullLogicalValidation;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.buttons.CollectionTableButtonsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.DefaultSortCriteriaConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 13.12.2014
 *         Time: 20:12
 */
@Root(name = "table-viewer")
public class TableViewerConfig extends WidgetConfig {
    @NotNullLogicalValidation
    @Element(name = "collection-view-ref", required = false)
    private CollectionViewRefConfig collectionViewRefConfig;

    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "page-size", required = false)
    private Integer pageSize;

    @Element(name = "default-sort-criteria", required = false)
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;

    @Element(name = "collection-extra-filters", required = false)
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;

    @Element(name = "collection-table-buttons", required = false)
    private CollectionTableButtonsConfig collectionTableButtonsConfig;

    public CollectionViewRefConfig getCollectionViewRefConfig() {
        return collectionViewRefConfig;
    }

    public void setCollectionViewRefConfig(CollectionViewRefConfig collectionViewRefConfig) {
        this.collectionViewRefConfig = collectionViewRefConfig;
    }

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
    }

    public CollectionExtraFiltersConfig getCollectionExtraFiltersConfig() {
        return collectionExtraFiltersConfig;
    }

    public void setCollectionExtraFiltersConfig(CollectionExtraFiltersConfig collectionExtraFiltersConfig) {
        this.collectionExtraFiltersConfig = collectionExtraFiltersConfig;
    }

    public CollectionTableButtonsConfig getCollectionTableButtonsConfig() {
        return collectionTableButtonsConfig;
    }

    public void setCollectionTableButtonsConfig(CollectionTableButtonsConfig collectionTableButtonsConfig) {
        this.collectionTableButtonsConfig = collectionTableButtonsConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableViewerConfig that = (TableViewerConfig) o;

        if (collectionExtraFiltersConfig != null ? !collectionExtraFiltersConfig.equals(that.collectionExtraFiltersConfig)
                : that.collectionExtraFiltersConfig != null) {
            return false;
        }
        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.collectionRefConfig)
                : that.collectionRefConfig != null) {
            return false;
        }
        if (collectionViewRefConfig != null ? !collectionViewRefConfig.equals(that.collectionViewRefConfig)
                : that.collectionViewRefConfig != null) {
            return false;
        }
        if (defaultSortCriteriaConfig != null ? !defaultSortCriteriaConfig.equals(that.defaultSortCriteriaConfig)
                : that.defaultSortCriteriaConfig != null) {
            return false;
        }
        if (pageSize != null ? !pageSize.equals(that.pageSize) : that.pageSize != null) {
            return false;
        }
        if (collectionTableButtonsConfig != null ? !collectionTableButtonsConfig.equals(that.collectionTableButtonsConfig)
                : that.collectionTableButtonsConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionViewRefConfig != null ? collectionViewRefConfig.hashCode() : 0;
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 0);
        result = 31 * result + (pageSize != null ? pageSize.hashCode() : 0);
        result = 31 * result + (defaultSortCriteriaConfig != null ? defaultSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (collectionExtraFiltersConfig != null ? collectionExtraFiltersConfig.hashCode() : 0);
        return result;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getComponentName() {
        return "table-viewer";
    }
}
