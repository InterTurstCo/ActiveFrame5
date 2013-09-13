package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.form.HeaderConfig;
import ru.intertrust.cm.core.config.model.gui.form.TabGroupConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 12.09.13
 * Time: 15:48
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "widget-config")
public class WidgetConfigurationConfig implements Dto {
    @ElementList(inline = true)
    private List<LabelConfig> labelConfigList = new ArrayList<>();

    @Element(name = "integer-box")
    private IntegerBoxConfig integerBoxConfig;

    @Element(name = "linked-domain-objects-editable-table")
    private LinkDomainObjectsEditableTableConfig linkDomainObjectsEditableTableConfig;

    @Element(name = "linked-domain-object-hyperlink")
    private LinkedDomainObjectHyperlinkConfig linkedDomainObjectHyperlinkConfig;

    @Element(name = "rich-text-area")
    private RichTextAreaConfig richTextAreaConfig;

    @Element(name = "linked-domain-objects-table")
    private LinkedDomainObjectsTableConfig linkedDomainObjectsTableConfig;

    @Element(name = "template-based-widget")
    private TemplateBasedWidgetConfig templateBasedWidgetConfig;

    public List<LabelConfig> getLabelConfigList() {
        return labelConfigList;
    }

    public void setLabelConfigList(List<LabelConfig> labelConfigList) {
        this.labelConfigList = labelConfigList;
    }

    public IntegerBoxConfig getIntegerBoxConfig() {
        return integerBoxConfig;
    }

    public void setIntegerBoxConfig(IntegerBoxConfig integerBoxConfig) {
        this.integerBoxConfig = integerBoxConfig;
    }

    public LinkDomainObjectsEditableTableConfig getLinkDomainObjectsEditableTableConfig() {
        return linkDomainObjectsEditableTableConfig;
    }

    public void setLinkDomainObjectsEditableTableConfig
            (LinkDomainObjectsEditableTableConfig linkDomainObjectsEditableTableConfig) {
                this.linkDomainObjectsEditableTableConfig = linkDomainObjectsEditableTableConfig;
    }

    public LinkedDomainObjectHyperlinkConfig getLinkedDomainObjectHyperlinkConfig() {
        return linkedDomainObjectHyperlinkConfig;
    }

    public void setLinkedDomainObjectHyperlinkConfig
            (LinkedDomainObjectHyperlinkConfig linkedDomainObjectHyperlinkConfig) {
                this.linkedDomainObjectHyperlinkConfig = linkedDomainObjectHyperlinkConfig;
    }

    public RichTextAreaConfig getRichTextAreaConfig() {
        return richTextAreaConfig;
    }

    public void setRichTextAreaConfig(RichTextAreaConfig richTextAreaConfig) {
        this.richTextAreaConfig = richTextAreaConfig;
    }

    public LinkedDomainObjectsTableConfig getLinkedDomainObjectsTableConfig() {
        return linkedDomainObjectsTableConfig;
    }

    public void setLinkedDomainObjectsTableConfig(LinkedDomainObjectsTableConfig linkedDomainObjectsTableConfig) {
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

        if (integerBoxConfig != null ? !integerBoxConfig.equals(that.integerBoxConfig) : that.
                integerBoxConfig != null) {
                    return false;
        }
        if (labelConfigList != null ? !labelConfigList.equals(that.labelConfigList) : that.labelConfigList != null) {
            return false;
        }
        if (linkDomainObjectsEditableTableConfig != null ? !linkDomainObjectsEditableTableConfig.
                equals(that.linkDomainObjectsEditableTableConfig) :that.linkDomainObjectsEditableTableConfig != null) {
                    return false;
        }
        if (linkedDomainObjectHyperlinkConfig != null ? !linkedDomainObjectHyperlinkConfig.
                equals(that.linkedDomainObjectHyperlinkConfig) : that.linkedDomainObjectHyperlinkConfig != null) {
            return false;
        }
        if (linkedDomainObjectsTableConfig != null ? !linkedDomainObjectsTableConfig.
                equals(that.linkedDomainObjectsTableConfig) :
                    that.linkedDomainObjectsTableConfig != null) {
            return false;
        }
        if (richTextAreaConfig != null ? !richTextAreaConfig.equals(that.richTextAreaConfig) : that.
                richTextAreaConfig != null) {
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
        int result = labelConfigList != null ? labelConfigList.hashCode() : 0;
        result = 31 * result + (integerBoxConfig != null ? integerBoxConfig.hashCode() : 0);
        result = result + (linkDomainObjectsEditableTableConfig !=
                null ? linkDomainObjectsEditableTableConfig.hashCode() : 0);
        result = result + (linkedDomainObjectHyperlinkConfig != null ? linkedDomainObjectHyperlinkConfig.hashCode() : 0);
        result = result + (richTextAreaConfig != null ? richTextAreaConfig.hashCode() : 0);
        result = result + (linkedDomainObjectsTableConfig != null ? linkedDomainObjectsTableConfig.hashCode() : 0);
        result = result + (templateBasedWidgetConfig != null ? templateBasedWidgetConfig.hashCode() : 0);
        return result;
    }
}
