package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 05.09.13
 *         Time: 15:58
 */
@Root( name = "form", strict = false)
public class FormConfig implements Dto, TopLevelConfig {

    public static final String TYPE_EDIT = "edit";
    public static final String TYPE_SEARCH = "search";
    public static final String TYPE_REPORT = "report";


    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "domain-object-type", required = false)
    private String domainObjectType;

    @Attribute(name = "is-default",required = false)
    private boolean isDefault;

    @Attribute(name = "debug",required = false)
    private boolean debug;

    @Attribute(name = "min-width",required = false)
    private String minWidth;

    @Attribute(name = "type",required = false)
    private String type;

    @Attribute(name = "report-template", required = false)
    private String reportTemplate;

    @Element(name = "tool-bar", required = false)
    private ToolBarConfig toolbarConfig;

    @Element(name = "markup")
    private MarkupConfig markup;

    @Element(name = "widget-config")
    private WidgetConfigurationConfig widgetConfigurationConfig;

    @Element(name = "form-objects-remover", required = false)
    private FormObjectsRemoverConfig formObjectsRemoverConfig;

    public String getType() {
        return type != null ? type : TYPE_EDIT;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MarkupConfig getMarkup() {
        return markup;
    }

    public void setMarkup(MarkupConfig markup) {
        this.markup = markup;
    }
    @Override
    public String getName() {
        return name;
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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(String minWidth) {
        this.minWidth = minWidth;
    }

    public WidgetConfigurationConfig getWidgetConfigurationConfig() {
        return widgetConfigurationConfig;
    }

    public void setWidgetConfigurationConfig(WidgetConfigurationConfig widgetConfigurationConfig) {
        this.widgetConfigurationConfig = widgetConfigurationConfig;
    }

    public FormObjectsRemoverConfig getFormObjectsRemoverConfig() {
        return formObjectsRemoverConfig;
    }

    public void setFormObjectsRemoverConfig(FormObjectsRemoverConfig formObjectsRemoverConfig) {
        this.formObjectsRemoverConfig = formObjectsRemoverConfig;
    }

    public ToolBarConfig getToolbarConfig() {
        return toolbarConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormConfig that = (FormConfig) o;

        if (isDefault != that.isDefault) {
            return false;
        }
        if (debug != that.debug) {
            return false;
        }
        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null) {
            return false;
        }
        if (markup != null ? !markup.equals(that.markup) : that.markup != null) {
            return false;
        }
        if (minWidth != null ? !minWidth.equals(that.minWidth) : that.minWidth != null) {
            return false;
        }

        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (widgetConfigurationConfig != null ? !widgetConfigurationConfig.equals(that.
                widgetConfigurationConfig) : that.widgetConfigurationConfig != null) {
            return false;
        }
        if (formObjectsRemoverConfig != null ? !formObjectsRemoverConfig.equals(that.
                formObjectsRemoverConfig) : that.formObjectsRemoverConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Form: name=")
                .append(name)
                .append(", type=")
                .append(type)
                .append(", domain-object-type=")
                .append(domainObjectType);
        return sb.toString();
    }

    public String getReportTemplate() {
        return reportTemplate;
    }

    public void setReportTemplate(String reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    /**
     * Возвращает имя "целевого объекта" формы, т.е. тип доменного объекта (для формы редактирования или поиска),
     * либо имя отчета (для формы отчета)
     * @return тип доменного объекта либо имя отчета
     */
    public String getTargetTypeName() {
        return TYPE_REPORT.equals(type) ? reportTemplate : domainObjectType;
    }

}
