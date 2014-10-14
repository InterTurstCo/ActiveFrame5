package ru.intertrust.cm.core.gui.api.server.form;

import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * Перехватчик точки расширения "перед сохранением формы".
 * @author Denis Mitavskiy
 *         Date: 13.10.2014
 *         Time: 16:48
 */
public interface FormBeforeSaveInterceptor extends ComponentHandler {
    /**
     * Метод вызываемый перед началом сохранения формы. Состояние формы можно изменять, это отразится на дальнейшем процессе сохранения
     * @param formState состояние формы, которая будет сохранена
     */
    void beforeSave(FormState formState);
}
