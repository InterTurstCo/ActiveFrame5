package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "widget-config")
public class WidgetConfigurationConfig implements Dto {
    @ElementListUnion({
            @ElementList(entry = "label", type = LabelConfig.class, inline = true, required = false),
            @ElementList(entry = "rich-text-area", type = TextBoxConfig.class, inline = true, required = false),
            @ElementList(entry = "integer-box", type = IntegerBoxConfig.class, inline = true, required = false),
            @ElementList(entry = "text-box", type = TextBoxConfig.class, inline = true, required = false),
            @ElementList(entry = "text-area", type = TextAreaConfig.class, inline = true, required = false),
            @ElementList(entry = "decimal-box", type = DecimalBoxConfig.class, inline = true, required = false),
            @ElementList(entry = "date-box", type = DateBoxConfig.class, inline = true, required = false),
            @ElementList(entry = "combo-box", type = ComboBoxConfig.class, inline = true, required = false)
    })
    private List<WidgetConfig> widgetConfigList = new ArrayList<WidgetConfig>();

    @ElementList(inline = true, required = false)
    private List<LinkDomainObjectsEditableTableConfig> linkDomainObjectsEditableTableConfigList =
            new ArrayList<LinkDomainObjectsEditableTableConfig>();

    @ElementList(inline = true, required = false)
    private List<LinkedDomainObjectHyperlinkConfig> linkedDomainObjectHyperlinkConfigList =
            new ArrayList<LinkedDomainObjectHyperlinkConfig>();

    @ElementList(inline = true, required = false)
    private List<LinkedDomainObjectsTableConfig> linkedDomainObjectsTableConfig =
            new ArrayList<LinkedDomainObjectsTableConfig>();

    @Element(name = "template-based-widget", required = false)
    private TemplateBasedWidgetConfig templateBasedWidgetConfig;

    public List<WidgetConfig> getWidgetConfigList() {
        return widgetConfigList;
    }

    public void setWidgetConfigList(List<WidgetConfig> widgetConfigList) {
        this.widgetConfigList = widgetConfigList;
    }

    public List<LinkDomainObjectsEditableTableConfig> getLinkDomainObjectsEditableTableConfigList() {
        return linkDomainObjectsEditableTableConfigList;
    }

    public void setLinkDomainObjectsEditableTableConfigList(List<LinkDomainObjectsEditableTableConfig>
                                                                    linkDomainObjectsEditableTableConfigList) {
        this.linkDomainObjectsEditableTableConfigList = linkDomainObjectsEditableTableConfigList;
    }

    public List<LinkedDomainObjectHyperlinkConfig> getLinkedDomainObjectHyperlinkConfigList() {
        return linkedDomainObjectHyperlinkConfigList;
    }

    public void setLinkedDomainObjectHyperlinkConfigList(List<LinkedDomainObjectHyperlinkConfig>
                                                                 linkedDomainObjectHyperlinkConfigList) {
        this.linkedDomainObjectHyperlinkConfigList = linkedDomainObjectHyperlinkConfigList;
    }

    public List<LinkedDomainObjectsTableConfig> getLinkedDomainObjectsTableConfig() {
        return linkedDomainObjectsTableConfig;
    }

    public void setLinkedDomainObjectsTableConfig(List<LinkedDomainObjectsTableConfig> linkedDomainObjectsTableConfig) {
        this.linkedDomainObjectsTableConfig = linkedDomainObjectsTableConfig;
    }

    public TemplateBasedWidgetConfig getTemplateBasedWidgetConfig() {
        return templateBasedWidgetConfig;
    }

    public void setTemplateBasedWidgetConfig(TemplateBasedWidgetConfig templateBasedWidgetConfig) {
        this.templateBasedWidgetConfig = templateBasedWidgetConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetConfigurationConfig that = (WidgetConfigurationConfig) o;

        if (widgetConfigList != null ? !widgetConfigList.equals(that.widgetConfigList) : that.
                widgetConfigList != null) {
            return false;
        }
        if (linkDomainObjectsEditableTableConfigList != null ? !linkDomainObjectsEditableTableConfigList.equals(that.
                linkDomainObjectsEditableTableConfigList) : that.linkDomainObjectsEditableTableConfigList != null) {
            return false;
        }
        if (linkedDomainObjectHyperlinkConfigList != null ? !linkedDomainObjectHyperlinkConfigList.equals(that.
                linkedDomainObjectHyperlinkConfigList) : that.linkedDomainObjectHyperlinkConfigList != null) {
            return false;
        }
        if (linkedDomainObjectsTableConfig != null ? !linkedDomainObjectsTableConfig.equals(that.
                linkedDomainObjectsTableConfig) : that.linkedDomainObjectsTableConfig != null) {
            return false;
        }
        if (templateBasedWidgetConfig != null ? !templateBasedWidgetConfig.equals(that.
                templateBasedWidgetConfig) : that.templateBasedWidgetConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = widgetConfigList != null ? widgetConfigList.hashCode() : 0;
        result = 31 * result + (linkDomainObjectsEditableTableConfigList != null ?
                linkDomainObjectsEditableTableConfigList.hashCode() : 0);
        result = 31 * result + (linkedDomainObjectHyperlinkConfigList != null ?
                linkedDomainObjectHyperlinkConfigList.hashCode() : 0);
        result = 31 * result + (linkedDomainObjectsTableConfig != null ? linkedDomainObjectsTableConfig.hashCode() : 0);
        result = 31 * result + (templateBasedWidgetConfig != null ? templateBasedWidgetConfig.hashCode() : 0);
        return result;
    }
}