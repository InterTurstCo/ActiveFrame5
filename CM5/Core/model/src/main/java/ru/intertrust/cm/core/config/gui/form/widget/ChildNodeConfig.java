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
@Root(name = "child-node")
public class ChildNodeConfig implements Dto {
    @Attribute(name = "domain-object-type", required = false)
    private String domainObjectType;

    @Attribute(name = "parent-link-field", required = false)
    private String parentLinkField;

    @Element (name = "node-collection-def")
    private NodeCollectionDefConfig nodeCollectionDefConfig;

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public String getParentLinkField() {
        return parentLinkField;
    }

    public void setParentLinkField(String parentLinkField) {
        this.parentLinkField = parentLinkField;
    }

    public NodeCollectionDefConfig getNodeCollectionDefConfig() {
        return nodeCollectionDefConfig;
    }

    public void setNodeCollectionDefConfig(NodeCollectionDefConfig nodeCollectionDefConfig) {
        this.nodeCollectionDefConfig = nodeCollectionDefConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChildNodeConfig that = (ChildNodeConfig) o;

        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null){
            return false;
        }

        if (nodeCollectionDefConfig != null ? !nodeCollectionDefConfig.equals(that.
                nodeCollectionDefConfig) : that.nodeCollectionDefConfig != null) {
            return false;
        }

        if (parentLinkField != null ? !parentLinkField.equals(that.parentLinkField) : that.parentLinkField != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = domainObjectType != null ? domainObjectType.hashCode() : 0;
        result = 31 * result + (parentLinkField != null ? parentLinkField.hashCode() : 0);
        result = 31 * result + (nodeCollectionDefConfig != null ? nodeCollectionDefConfig.hashCode() : 0);
        return result;
    }
}
