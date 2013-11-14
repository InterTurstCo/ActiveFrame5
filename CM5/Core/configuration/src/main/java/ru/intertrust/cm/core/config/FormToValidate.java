package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;

import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/11/13
 *         Time: 13:05 PM
 */
public class FormToValidate {
    private String domainObjectType;
    private FieldConfig fieldConfig;
    private String componentName;
    private String formName;
    private MarkupConfig markup;
    private List<WidgetConfig> widgetConfigs;

    public List<WidgetConfig> getWidgetConfigs() {
        return widgetConfigs;
    }

    public void setWidgetConfigs(List<WidgetConfig> widgetConfigs) {
        this.widgetConfigs = widgetConfigs;
    }

    public MarkupConfig getMarkup() {
        return markup;
    }

    public void setMarkup(MarkupConfig markup) {
        this.markup = markup;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public FieldConfig getFieldConfig() {
        return fieldConfig;
    }

    public void setFieldConfig(FieldConfig fieldConfig) {
        this.fieldConfig = fieldConfig;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }
}
