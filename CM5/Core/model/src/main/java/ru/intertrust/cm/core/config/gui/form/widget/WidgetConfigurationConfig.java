package ru.intertrust.cm.core.config.gui.form.widget;

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
            @ElementList(entry = "combo-box", type = ComboBoxConfig.class, inline = true, required = false),
            @ElementList(entry = "radio-button", type = RadioButtonConfig.class, inline = true, required = false),
            @ElementList(entry = "linked-domain-object-hyperlink",
                    type = LinkedDomainObjectHyperlinkConfig.class, inline = true, required = false),
            @ElementList(entry = "linked-domain-objects-editable-table",
                    type = LinkDomainObjectsEditableTableConfig.class, inline = true, required = false),
            @ElementList(entry = "linked-domain-objects-table",
                    type = LinkedDomainObjectsTableConfig.class, inline = true, required = false),
            @ElementList(entry = "template-based-widget",
                    type = TemplateBasedWidgetConfig.class, inline = true, required = false),
            @ElementList(entry = "list-box", type = ListBoxConfig.class, inline = true, required = false),
            @ElementList(entry = "suggest-box", type = SuggestBoxConfig.class, inline = true, required = false),
            @ElementList(entry = "check-box", type = CheckBoxConfig.class, inline = true, required = false),
            @ElementList(entry = "table-browser", type = TableBrowserConfig.class, inline = true, required = false),
            @ElementList(entry = "attachment-box", type = AttachmentBoxConfig.class, inline = true, required = false),
            @ElementList(entry = "hierarchy-browser", type = HierarchyBrowserConfig.class, inline = true, required = false)
    })
    private List<WidgetConfig> widgetConfigList = new ArrayList<WidgetConfig>();

    public List<WidgetConfig> getWidgetConfigList() {
        return widgetConfigList;
    }

    public void setWidgetConfigList(List<WidgetConfig> widgetConfigList) {
        this.widgetConfigList = widgetConfigList;
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

        return true;
    }

    @Override
    public int hashCode() {
        int result = widgetConfigList != null ? widgetConfigList.hashCode() : 0;

        return result;
    }
}