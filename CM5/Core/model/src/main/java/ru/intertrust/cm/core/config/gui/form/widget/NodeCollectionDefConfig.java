package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 11:40
 */
@Root(name = "node-collection-def")
public class NodeCollectionDefConfig implements Dto {
    @Attribute(name = "collection")
    private String collection;

    @Attribute(name = "parent-filter", required = false)
    private String parentFilter;

    @Attribute(name = "selective", required = false)
    private boolean selective = true;

    @Attribute(name = "domain-object-type", required = false)
    private String domainObjectType;

    @Element(name = "selection-pattern", required = false)
    private SelectionPatternConfig selectionPatternConfig;

    @Element(name = "input-text-filter", required = false)
    private InputTextFilterConfig inputTextFilterConfig;

    @Element(name = "node-collection-def", required = false)
    private NodeCollectionDefConfig nodeCollectionDefConfig;

    public SelectionPatternConfig getSelectionPatternConfig() {
        return selectionPatternConfig;
    }

    public void setSelectionPatternConfig(SelectionPatternConfig selectionPatternConfig) {
        this.selectionPatternConfig = selectionPatternConfig;
    }

    public boolean isSelective() {
        return selective;
    }

    public void setSelective(boolean selective) {
        this.selective = selective;
    }

    public InputTextFilterConfig getInputTextFilterConfig() {
        return inputTextFilterConfig;
    }

    public void setInputTextFilterConfig(InputTextFilterConfig inputTextFilterConfig) {
        this.inputTextFilterConfig = inputTextFilterConfig;
    }

    public NodeCollectionDefConfig getNodeCollectionDefConfig() {
        return nodeCollectionDefConfig;
    }

    public void setNodeCollectionDefConfig(NodeCollectionDefConfig nodeCollectionDefConfig) {
        this.nodeCollectionDefConfig = nodeCollectionDefConfig;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getParentFilter() {
        return parentFilter;
    }

    public void setParentFilter(String parentFilter) {
        this.parentFilter = parentFilter;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NodeCollectionDefConfig that = (NodeCollectionDefConfig) o;

        if (nodeCollectionDefConfig != null ? !nodeCollectionDefConfig.
                equals(that.nodeCollectionDefConfig) : that.nodeCollectionDefConfig != null) {
            return false;
        }

        if (inputTextFilterConfig != null ? !inputTextFilterConfig.equals(that.
                inputTextFilterConfig) : that.inputTextFilterConfig != null) {
            return false;
        }
        if (selectionPatternConfig != null ? !selectionPatternConfig.equals(that.
                selectionPatternConfig) : that.selectionPatternConfig != null) {
            return false;
        }
        if (parentFilter != null ? !parentFilter.equals(that.parentFilter) : that.parentFilter != null) {
            return false;
        }
        if (collection != null ? !collection.equals(that.collection) : that.collection != null) {
            return false;
        }
        if (selective != that.selective) {
            return false;
        }
        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = selectionPatternConfig != null ? selectionPatternConfig.hashCode() : 0;
        result = 31 * result + (inputTextFilterConfig != null ? inputTextFilterConfig.hashCode() : 0);
        result = 31 * result + (nodeCollectionDefConfig != null ? nodeCollectionDefConfig.hashCode() : 0);
        result = 31 * result + (parentFilter != null ? parentFilter.hashCode() : 0);
        result = 31 * result + (collection != null ? collection.hashCode() : 0);
        result = 31 * result +(selective ? 1 : 0);
        result = 31 * result + (domainObjectType != null ? domainObjectType.hashCode() : 0);
        return result;
    }
}
