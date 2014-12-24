package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "summary-table-column")
public class SummaryTableColumnConfig extends ColumnParentConfig {

    @Attribute(name = "widget-id", required = false)
    private String widgetId;

    @Element(name="action",required = false)
    private SummaryTableActionColumnConfig summaryTableActionColumnConfig;

    @Element(name="widget-id-mappings",required = false)
    private WidgetIdMappingsConfig widgetIdMappingsConfig;

    @ElementList(name = "pattern", required = false, entry = "pattern", inline = true)
    private List<LinkedTablePatternConfig> patternConfig = new ArrayList<>();

    @Element(name = "formatting", required = false)
    private FormattingConfig formattingConfig;

    @Attribute(name = "column-renderer-component",required = false)
    private String valueGeneratorComponent;

    public List<LinkedTablePatternConfig> getPatternConfig() {
        return patternConfig;
    }

    public void setPatternConfig(List<LinkedTablePatternConfig> patternConfig) {
        this.patternConfig = patternConfig;
    }

    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SummaryTableColumnConfig that = (SummaryTableColumnConfig) o;

        if (formattingConfig != null ? !formattingConfig.equals(that.formattingConfig) : that.formattingConfig != null)
            return false;
        if (patternConfig != null ? !patternConfig.equals(that.patternConfig) : that.patternConfig != null)
            return false;
        if (widgetId != null ? !widgetId.equals(that.widgetId) : that.widgetId != null) return false;

        return true;
    }

    public WidgetIdMappingsConfig getWidgetIdMappingsConfig() {
        return widgetIdMappingsConfig;
    }

    public void setWidgetIdMappingsConfig(WidgetIdMappingsConfig widgetIdMappingsConfig) {
        this.widgetIdMappingsConfig = widgetIdMappingsConfig;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (widgetId != null ? widgetId.hashCode() : 0);
        result = 31 * result + (patternConfig != null ? patternConfig.hashCode() : 0);
        result = 31 * result + (formattingConfig != null ? formattingConfig.hashCode() : 0);
        return result;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public String getValueGeneratorComponent() {
        return valueGeneratorComponent;
    }

    public void setValueGeneratorComponent(String valueGeneratorComponent) {
        this.valueGeneratorComponent = valueGeneratorComponent;
    }

    public SummaryTableActionColumnConfig getSummaryTableActionColumnConfig() {
        return summaryTableActionColumnConfig;
    }

    public void setSummaryTableActionColumnConfig(SummaryTableActionColumnConfig summaryTableActionColumnConfig) {
        this.summaryTableActionColumnConfig = summaryTableActionColumnConfig;
    }
}
