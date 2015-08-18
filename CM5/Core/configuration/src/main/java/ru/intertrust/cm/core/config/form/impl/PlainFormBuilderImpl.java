package ru.intertrust.cm.core.config.form.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.form.PlainFormBuilder;
import ru.intertrust.cm.core.config.form.processor.FormExtensionsProcessor;
import ru.intertrust.cm.core.config.form.processor.WidgetTemplateProcessor;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.util.ObjectCloner;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.05.2015
 *         Time: 18:23
 */
public class PlainFormBuilderImpl implements PlainFormBuilder {

    @Autowired
    private FormExtensionsProcessor formExtensionsProcessor;

    @Autowired
    private WidgetTemplateProcessor widgetTemplateProcessor;

    private ObjectCloner clonePerformer;

    public PlainFormBuilderImpl() {
        this.clonePerformer = ObjectCloner.getInstance();
    }

    @Override
    public FormConfig buildPlainForm(FormConfig rawFormConfig) {
        FormConfig result = clonePerformer.cloneObject(rawFormConfig);
        if (formExtensionsProcessor.hasExtensions(rawFormConfig)) {
            result = formExtensionsProcessor.processExtensions(result);
        }
        List<WidgetConfig> widgetConfigs = result.getWidgetConfigurationConfig().getWidgetConfigList();
        if (widgetTemplateProcessor.hasTemplateBasedWidgets(widgetConfigs)) {
            List<WidgetConfig> processedWidgets = widgetTemplateProcessor.processTemplates(result.getName(), widgetConfigs);
            result.getWidgetConfigurationConfig().setWidgetConfigList(processedWidgets);
        }
        return result;
    }

    @Override
    public boolean isRaw(FormConfig formConfig) {
        return formExtensionsProcessor.hasExtensions(formConfig)
                || widgetTemplateProcessor.hasTemplateBasedWidgets(formConfig.getWidgetConfigurationConfig().getWidgetConfigList());
    }

}
