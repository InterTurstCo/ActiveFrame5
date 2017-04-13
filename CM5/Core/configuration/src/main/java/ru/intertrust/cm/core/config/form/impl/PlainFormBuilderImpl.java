package ru.intertrust.cm.core.config.form.impl;

import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.form.PlainFormBuilder;
import ru.intertrust.cm.core.config.form.processor.FormExtensionsProcessor;
import ru.intertrust.cm.core.config.form.processor.FormTemplateProcessor;
import ru.intertrust.cm.core.config.form.processor.impl.FormExtensionsProcessorImpl;
import ru.intertrust.cm.core.config.form.processor.impl.TabTemplateProcessor;
import ru.intertrust.cm.core.config.form.processor.impl.TableTemplateProcessor;
import ru.intertrust.cm.core.config.form.processor.impl.WidgetTemplateProcessorImpl;
import ru.intertrust.cm.core.config.gui.form.FormConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.05.2015
 *         Time: 18:23
 */
public class PlainFormBuilderImpl implements PlainFormBuilder {
    private FormExtensionsProcessor formExtensionsProcessor;
    private FormTemplateProcessor widgetTemplateProcessor;
    private FormTemplateProcessor tabTemplateProcessor;
    private FormTemplateProcessor tableTemplateProcessor;

    public PlainFormBuilderImpl() {
    }

    public PlainFormBuilderImpl(ConfigurationExplorer explorer) {
        formExtensionsProcessor = new FormExtensionsProcessorImpl(explorer);
        widgetTemplateProcessor = new WidgetTemplateProcessorImpl(explorer);
        tabTemplateProcessor = new TabTemplateProcessor(explorer);
        tableTemplateProcessor = new TableTemplateProcessor(explorer);
    }

    @Override
    public FormConfig buildPlainForm(FormConfig rawFormConfig) {
        FormConfig result = ObjectCloner.getInstance().cloneObject(rawFormConfig);
        if (formExtensionsProcessor.hasExtensions(rawFormConfig)) {
            result = formExtensionsProcessor.processExtensions(result);
        }
        if(tabTemplateProcessor.hasTemplateBasedElements(result)){
            result = tabTemplateProcessor.processTemplates(result);
        }
        if(tableTemplateProcessor.hasTemplateBasedElements(result)){
            result = tableTemplateProcessor.processTemplates(result);
        }
        if (widgetTemplateProcessor.hasTemplateBasedElements(result)) {
            result = widgetTemplateProcessor.processTemplates(result);
        }
        return result;
    }

    @Override
    public boolean isRaw(FormConfig formConfig) {
        return formExtensionsProcessor.hasExtensions(formConfig)
                || widgetTemplateProcessor.hasTemplateBasedElements(formConfig)
                || tableTemplateProcessor.hasTemplateBasedElements(formConfig)
                || tabTemplateProcessor.hasTemplateBasedElements(formConfig);
    }

}
