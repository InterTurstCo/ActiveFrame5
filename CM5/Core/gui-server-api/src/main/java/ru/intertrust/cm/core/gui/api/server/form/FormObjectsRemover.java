package ru.intertrust.cm.core.gui.api.server.form;

import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * @author Denis Mitavskiy
 *         Date: 22.07.2014
 *         Time: 13:35
 */
public interface FormObjectsRemover extends ComponentHandler {
    void deleteForm(FormState currentFormState);
}
