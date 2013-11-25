package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 11:40
 */
@Root(name = "table-browser")
public class TableBrowserConfig extends WidgetConfig {
    @Element(name = "collection-view-ref", required = false)
    private CollectionViewRefConfig collectionViewRefConfig;

    @Element(name = "collection-ref", required = false)
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "selection-filter", required = false)
    private SelectionFilterConfig selectionFilterConfig;

    @Element(name = "selection-exclude-filter", required = false)
    private SelectionExcludeFilterConfig selectionExcludeFilterConfig;

    @Element(name = "selection-pattern", required = false)
    private SelectionPatternConfig selectionPatternConfig;

    @Element(name = "input-text-filter", required = false)
    private InputTextFilterConfig inputTextFilterConfig;

    @Element(name = "page-size", required = false)
    private Integer pageSize;

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

    public SelectionFilterConfig getSelectionFilterConfig() {
        return selectionFilterConfig;
    }

    public void setSelectionFilterConfig(SelectionFilterConfig selectionFilterConfig) {
        this.selectionFilterConfig = selectionFilterConfig;
    }

    public SelectionExcludeFilterConfig getSelectionExcludeFilterConfig() {
        return selectionExcludeFilterConfig;
    }

    public void setSelectionExcludeFilterConfig(SelectionExcludeFilterConfig selectionExcludeFilterConfig) {
        this.selectionExcludeFilterConfig = selectionExcludeFilterConfig;
    }

    public SelectionPatternConfig getSelectionPatternConfig() {
        return selectionPatternConfig;
    }

    public void setSelectionPatternConfig(SelectionPatternConfig selectionPatternConfig) {
        this.selectionPatternConfig = selectionPatternConfig;
    }

    public InputTextFilterConfig getInputTextFilterConfig() {
        return inputTextFilterConfig;
    }

    public void setInputTextFilterConfig(InputTextFilterConfig inputTextFilterConfig) {
        this.inputTextFilterConfig = inputTextFilterConfig;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        TableBrowserConfig that = (TableBrowserConfig) o;

        if (collectionRefConfig != null ? !collectionRefConfig.equals(that.collectionRefConfig) :
                that.collectionRefConfig != null) {
            return false;
        }
        if (collectionViewRefConfig != null ? !collectionViewRefConfig.equals(that.collectionViewRefConfig) :
                that.collectionViewRefConfig != null) {
            return false;
        }
        if (inputTextFilterConfig != null ? !inputTextFilterConfig.equals(that.inputTextFilterConfig) :
                that.inputTextFilterConfig != null) {
            return false;
        }
        if (pageSize != null ? !pageSize.equals(that.pageSize) : that.pageSize != null) {
            return false;
        }

        if (selectionExcludeFilterConfig != null ? !selectionExcludeFilterConfig.
                equals(that.selectionExcludeFilterConfig) : that.selectionExcludeFilterConfig != null) {
            return false;
        }
        if (selectionFilterConfig != null ? !selectionFilterConfig.equals(that.selectionFilterConfig) :
                that.selectionFilterConfig != null) {
            return false;
        }
        if (selectionPatternConfig != null ? !selectionPatternConfig.equals(that.selectionPatternConfig) :
                that.selectionPatternConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (collectionViewRefConfig != null ? collectionViewRefConfig.hashCode() : 0);
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 0);
        result = 31 * result + (selectionFilterConfig != null ? selectionFilterConfig.hashCode() : 0);
        result = 31 * result + (selectionExcludeFilterConfig != null ? selectionExcludeFilterConfig.hashCode() : 0);
        result = 31 * result + (selectionPatternConfig != null ? selectionPatternConfig.hashCode() : 0);
        result = 31 * result + (inputTextFilterConfig != null ? inputTextFilterConfig.hashCode() : 0);
        result = 31 * result + (pageSize != null ? pageSize.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "table-browser";
    }
}
