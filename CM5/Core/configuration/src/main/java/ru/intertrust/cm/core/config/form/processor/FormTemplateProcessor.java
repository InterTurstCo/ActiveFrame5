package ru.intertrust.cm.core.config.form.processor;

import ru.intertrust.cm.core.config.gui.form.FormConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.08.2015
 *         Time: 15:08
 */
public interface FormTemplateProcessor {
    FormConfig processTemplates(FormConfig formConfig);

    boolean hasTemplateBasedElements(FormConfig formConfig);
}
