package ru.intertrust.cm.core.config.form;

import ru.intertrust.cm.core.config.gui.form.FormConfig;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.05.2015
 *         Time: 16:37
 */
public interface PlainFormBuilder {
    /**
     * Создает конфигурацию формы, готовую для использования механизмом форм, учитыая наследование и расширение
     * @param rawFormConfig конфигурация формы, которую нужно собрать, учитыая наследование и расширение
     * @param formConfigs Конфигурации родительских форм, рутовая родительская конфигурация - в начале списка.
     * @return конфигурацию собранной формы
     * @throws ru.intertrust.cm.core.config.ConfigurationException если конфигурацию формы невозможно собрать
     */
    FormConfig buildPlainForm(FormConfig rawFormConfig, List<FormConfig> formConfigs);

    /**
     * Проверяет, нужно ли собрать форму, готовую для использования механизмом форм
     * @param formConfig
     * @return true - если нужно собирать, false - не нужно собирать
     */
    boolean isRaw(FormConfig formConfig);
}
