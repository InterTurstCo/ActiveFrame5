package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveHashMap;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.LocalizableConfig;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.form.extension.markup.MarkupExtensionConfig;
import ru.intertrust.cm.core.config.gui.form.extension.widget.configuration.WidgetConfigurationExtensionConfig;
import ru.intertrust.cm.core.config.gui.form.extension.widget.groups.WidgetGroupsExtensionConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfigurationConfig;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 05.09.13
 *         Time: 15:58
 */
@Root(name = "form", strict = false)
public class FormConfig implements Dto, TopLevelConfig, LocalizableConfig {

    public static final String TYPE_EDIT = "edit";
    public static final String TYPE_SEARCH = "search";
    public static final String TYPE_REPORT = "report";


    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Attribute(name = "domain-object-type", required = false)
    private String domainObjectType;

    @Attribute(name = "is-default", required = false)
    private Boolean isDefault;

    @Attribute(name = "debug", required = false)
    private Boolean debug;

    @Attribute(name = "min-width", required = false)
    private String minWidth;

    @Attribute(name = "type", required = false)
    private String type;

    @Attribute(name = "report-template", required = false)
    private String reportTemplate;

    @Attribute(name = "default-value-setter", required = false)
    private String defaultValueSetter;

    @Attribute(name = "re-read-in-same-transaction", required = false)
    private Boolean reReadInSameTransaction;

    @Attribute(name = "extends", required = false)
    private String parent;

    @Element(name = "tool-bar", required = false)
    private ToolBarConfig toolbarConfig;

    @Element(name = "markup", required = false)
    private MarkupConfig markup;

    @Element(name = "markup-extension", required = false)
    private MarkupExtensionConfig markupExtensionConfig;

    @Element(name = "widget-config", required = false)
    private WidgetConfigurationConfig widgetConfigurationConfig;

    @Element(name = "widget-config-extension", required = false)
    private WidgetConfigurationExtensionConfig widgetConfigurationExtensionConfig;

    @Element(name = "form-objects-remover", required = false)
    private FormObjectsRemoverConfig formObjectsRemoverConfig;

    @Element(name = "form-save-extension", required = false)
    private FormSaveExtensionConfig formSaveExtensionConfig;

    @Element(name = "widget-groups", required = false)
    private WidgetGroupsConfig widgetGroupsConfig;

    @Element(name = "widget-groups-extension", required = false)
    private WidgetGroupsExtensionConfig widgetGroupsExtensionConfig;

    @Element(name = "default-value-setter", required = false)
    private DefaultValueSetterConfig defaultValueSetterConfig;

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

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
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
        return isDefault == null ? false : isDefault;
    }

    public Boolean getNotSafeIsDefault(){
        return isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean getDebug() {
        return debug == null ? false : debug;
    }

    public Boolean getNotSafeIsDebug(){
        return debug;
    }

    public void setDebug(Boolean debug) {
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

    public WidgetConfigurationExtensionConfig getWidgetConfigurationExtensionConfig() {
        return widgetConfigurationExtensionConfig;
    }

    public void setWidgetConfigurationExtensionConfig(WidgetConfigurationExtensionConfig widgetConfigurationExtensionConfig) {
        this.widgetConfigurationExtensionConfig = widgetConfigurationExtensionConfig;
    }

    public FormObjectsRemoverConfig getFormObjectsRemoverConfig() {
        return formObjectsRemoverConfig;
    }

    public void setFormObjectsRemoverConfig(FormObjectsRemoverConfig formObjectsRemoverConfig) {
        this.formObjectsRemoverConfig = formObjectsRemoverConfig;
    }

    public FormSaveExtensionConfig getFormSaveExtensionConfig() {
        return formSaveExtensionConfig;
    }

    public void setFormSaveExtensionConfig(FormSaveExtensionConfig formSaveExtensionConfig) {
        this.formSaveExtensionConfig = formSaveExtensionConfig;
    }

    public ToolBarConfig getToolbarConfig() {
        return toolbarConfig;
    }

    public WidgetGroupsConfig getWidgetGroupsConfig() {
        return widgetGroupsConfig;
    }

    public String getDefaultValueSetter() {
        return defaultValueSetter;
    }

    public void setDefaultValueSetter(String defaultValueSetter) {
        this.defaultValueSetter = defaultValueSetter;
    }

    public DefaultValueSetterConfig getDefaultValueSetterConfig() {
        return defaultValueSetterConfig;
    }

    public void setDefaultValueSetterConfig(DefaultValueSetterConfig defaultValueSetterConfig) {
        this.defaultValueSetterConfig = defaultValueSetterConfig;
    }

    public Boolean reReadInSameTransaction() {
        return reReadInSameTransaction == null ? false : reReadInSameTransaction;
    }

    public void setReReadInSameTransaction(Boolean reReadInSameTransaction) {
        this.reReadInSameTransaction = reReadInSameTransaction;
    }

    public MarkupExtensionConfig getMarkupExtensionConfig() {
        return markupExtensionConfig;
    }

    public void setMarkupExtensionConfig(MarkupExtensionConfig markupExtensionConfig) {
        this.markupExtensionConfig = markupExtensionConfig;
    }

    public WidgetGroupsExtensionConfig getWidgetGroupsExtensionConfig() {
        return widgetGroupsExtensionConfig;
    }

    public void setWidgetGroupsExtensionConfig(WidgetGroupsExtensionConfig widgetGroupsExtensionConfig) {
        this.widgetGroupsExtensionConfig = widgetGroupsExtensionConfig;
    }

    public String getExtends() {
        return parent;
    }

    public void setExtends(String parent) {
        this.parent = parent;
    }

    public CaseInsensitiveHashMap<WidgetConfig> getWidgetConfigsById() {
        if (widgetConfigurationConfig == null) {
            return new CaseInsensitiveHashMap<>(0);
        }
        List<WidgetConfig> widgetConfigs = widgetConfigurationConfig.getWidgetConfigList();
        if (widgetConfigs == null) {
            return new CaseInsensitiveHashMap<>(0);
        }
        CaseInsensitiveHashMap<WidgetConfig> widgetConfigsById = new CaseInsensitiveHashMap<>(widgetConfigs.size());
        for (WidgetConfig config : widgetConfigs) {
            widgetConfigsById.put(config.getId(), config);
        }
        return widgetConfigsById;
    }

    public void setWidgetGroupsConfig(WidgetGroupsConfig widgetGroupsConfig) {
        this.widgetGroupsConfig = widgetGroupsConfig;
    }

    public void setToolbarConfig(ToolBarConfig toolbarConfig) {
        this.toolbarConfig = toolbarConfig;
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
        if (isDefault!= null ? !isDefault.equals(that.isDefault) : that.isDefault != null) {
            return false;
        }
        if (debug != null ? !debug.equals(that.debug) : that.debug != null) {
            return false;
        }
        if (reReadInSameTransaction != null ? !reReadInSameTransaction.equals(that.reReadInSameTransaction)
                : that.reReadInSameTransaction != null) {
            return false;
        }
        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null) {
            return false;
        }
        if (markup != null ? !markup.equals(that.markup) : that.markup != null) {
            return false;
        }
        if (markupExtensionConfig != null ? !markupExtensionConfig.equals(that.markupExtensionConfig)
                : that.markupExtensionConfig != null) {
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
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) {
            return false;
        }
        if (defaultValueSetter != null ? !defaultValueSetter.equals(that.defaultValueSetter) : that.defaultValueSetter != null) {
            return false;
        }
        if (parent != null ? !parent.equals(that.parent) : that.parent != null) {
            return false;
        }
        if (widgetConfigurationConfig != null ? !widgetConfigurationConfig.equals(that.
                widgetConfigurationConfig) : that.widgetConfigurationConfig != null) {
            return false;
        }
        if (widgetConfigurationExtensionConfig != null ? !widgetConfigurationExtensionConfig.equals(that.
                widgetConfigurationExtensionConfig) : that.widgetConfigurationExtensionConfig != null) {
            return false;
        }
        if (formObjectsRemoverConfig != null ? !formObjectsRemoverConfig.equals(that.
                formObjectsRemoverConfig) : that.formObjectsRemoverConfig != null) {
            return false;
        }
        if (widgetGroupsConfig != null ? !widgetGroupsConfig.equals(that.widgetGroupsConfig) :
                that.widgetGroupsConfig != null) {
            return false;
        }
        if (formSaveExtensionConfig != null ? !formSaveExtensionConfig.equals(that.formSaveExtensionConfig) :
                that.formSaveExtensionConfig != null) {
            return false;
        }
        if (defaultValueSetterConfig != null ? !defaultValueSetterConfig.equals(that.defaultValueSetterConfig) :
                that.defaultValueSetterConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        // gentlemen, no need to include everything in hash code, it slows down its calculation! "unique" identifiers are enough
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
     *
     * @return тип доменного объекта либо имя отчета
     */
    public String getTargetTypeName() {
        return TYPE_REPORT.equals(type) ? reportTemplate : domainObjectType;
    }

}
