package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 11:40
 */
@Root(name = "hierarchy-browser")
public class HierarchyBrowserConfig extends WidgetConfig {

    @Element(name = "node-collection-def")
    private NodeCollectionDefConfig nodeCollectionDefConfig;

    @Element(name = "page-size",required = false)
    private Integer pageSize;

    @Element(name = "single-choice", required = false)
    private SingleChoiceConfig singleChoice;

    @Element(name = "clear-all-button", required = false)
    ClearAllButtonConfig clearAllButtonConfig;

    @Element(name = "add-button", required = false)
    AddButtonConfig addButtonConfig;

    public NodeCollectionDefConfig getNodeCollectionDefConfig() {
        return nodeCollectionDefConfig;
    }

    public void setNodeCollectionDefConfig(NodeCollectionDefConfig nodeCollectionDefConfig) {
        this.nodeCollectionDefConfig = nodeCollectionDefConfig;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public SingleChoiceConfig getSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(SingleChoiceConfig singleChoice) {
        this.singleChoice = singleChoice;
    }

    public ClearAllButtonConfig getClearAllButtonConfig() {
        return clearAllButtonConfig;
    }

    public void setClearAllButtonConfig(ClearAllButtonConfig clearAllButtonConfig) {
        this.clearAllButtonConfig = clearAllButtonConfig;
    }

    public AddButtonConfig getAddButtonConfig() {
        return addButtonConfig;
    }

    public void setAddButtonConfig(AddButtonConfig addButtonConfig) {
        this.addButtonConfig = addButtonConfig;
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

        HierarchyBrowserConfig that = (HierarchyBrowserConfig) o;

        if (nodeCollectionDefConfig != null ? !nodeCollectionDefConfig.equals(that.
                nodeCollectionDefConfig) : that.nodeCollectionDefConfig != null) {
            return false;
        }
        if (pageSize != null ? !pageSize.equals(that.pageSize) : that.pageSize != null) {
            return false;
        }
        if (singleChoice != null ? !singleChoice.equals(that.singleChoice) :
                that.singleChoice != null) {
            return false;
        }

        if (clearAllButtonConfig != null ? !clearAllButtonConfig.equals(that.clearAllButtonConfig) :
                that.clearAllButtonConfig != null) {
            return false;
        }

        if (addButtonConfig != null ? !addButtonConfig.equals(that.addButtonConfig) :
                that.addButtonConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (singleChoice != null ? singleChoice.hashCode() : 0);
        result = 31 * result + (nodeCollectionDefConfig != null ? nodeCollectionDefConfig.hashCode() : 0);
        result = 31 * result + (pageSize != null ? pageSize.hashCode() : 0);
        result = 31 * result + (clearAllButtonConfig != null ? clearAllButtonConfig.hashCode() : 0);
        result = 31 * result + (addButtonConfig != null ? addButtonConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String getComponentName() {
        return "hierarchy-browser";
    }
}
