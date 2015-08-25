package ru.intertrust.cm.core.config.form.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.intertrust.cm.core.config.form.PlainFormBuilder;
import ru.intertrust.cm.core.config.form.processor.FormExtensionsProcessor;
import ru.intertrust.cm.core.config.form.processor.FormTemplateProcessor;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.util.ObjectCloner;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.05.2015
 *         Time: 18:23
 */
public class PlainFormBuilderImpl implements PlainFormBuilder {

    @Autowired
    private FormExtensionsProcessor formExtensionsProcessor;

    @Autowired
    @Qualifier("widgetTemplateProcessor")
    private FormTemplateProcessor widgetTemplateProcessor;

    @Autowired
    @Qualifier("tabTemplateProcessor")
    private FormTemplateProcessor tabTemplateProcessor;

    @Autowired
    @Qualifier("tableTemplateProcessor")
    private FormTemplateProcessor tableTemplateProcessor;

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
