package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetTemplateConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root
public class FormsParenConf {
    @Element(name = "form")
    private FormConfig formConfig;

    @Element(name = "widget-template")
    private ru.intertrust.cm.core.config.gui.form.widget.WidgetTemplateConfig widgetTemplateConfig;

    @Element(name = "form-mappings")
    private FormMappingsConfig formMappingsConfig;

    public FormConfig getFormConfig() {
        return formConfig;
    }

    public void setFormConfig(FormConfig formConfig) {
        this.formConfig = formConfig;
    }

    public WidgetTemplateConfig getWidgetTemplateConfig() {
        return widgetTemplateConfig;
    }

    public void setWidgetTemplateConfig(WidgetTemplateConfig widgetTemplateConfig) {
        this.widgetTemplateConfig = widgetTemplateConfig;
    }

    public FormMappingsConfig getFormMappingsConfig() {
        return formMappingsConfig;
    }

    public void setFormMappingsConfig(FormMappingsConfig formMappingsConfig) {
        this.formMappingsConfig = formMappingsConfig;
    }
}
