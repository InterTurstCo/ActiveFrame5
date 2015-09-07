package ru.intertrust.cm.core.config.form.processor;

import ru.intertrust.cm.core.config.gui.form.FormConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.08.2015
 *         Time: 9:49
 */
public interface FormExtensionsProcessor {
    FormConfig processExtensions(FormConfig formConfig);
    boolean hasExtensions(FormConfig formConfig);
}
