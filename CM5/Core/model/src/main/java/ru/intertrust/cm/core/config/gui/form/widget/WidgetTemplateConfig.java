package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 16/9/13
 *         Time: 12:05 PM
 */
@Root(name = "widget-template")
public class WidgetTemplateConfig implements Dto, TopLevelConfig {
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "domain-object-type", required = false)
    private String domainObjectType;

    @Element(name = "linked-domain-objects-editable-table")
    private LinkDomainObjectsEditableTableConfig linkDomainObjectsEditableTableConfig;

    @Override
    public String getName() {
        return name != null ? name : "widget-template-config";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public LinkDomainObjectsEditableTableConfig getLinkDomainObjectsEditableTableConfig() {
        return linkDomainObjectsEditableTableConfig;
    }

    public void setLinkDomainObjectsEditableTableConfig(LinkDomainObjectsEditableTableConfig linkDomainObjectsEditableTableConfig) {
        this.linkDomainObjectsEditableTableConfig = linkDomainObjectsEditableTableConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetTemplateConfig that = (WidgetTemplateConfig) o;

        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null) {
            return false;
        }
        if (linkDomainObjectsEditableTableConfig != null ? !linkDomainObjectsEditableTableConfig.equals(that.
                linkDomainObjectsEditableTableConfig) : that.linkDomainObjectsEditableTableConfig != null) {
            return false;
        }

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (domainObjectType != null ? domainObjectType.hashCode() : 0);
        result = 31 * result + (linkDomainObjectsEditableTableConfig != null ? linkDomainObjectsEditableTableConfig.
                hashCode() : 0);
        return result;
    }
}
