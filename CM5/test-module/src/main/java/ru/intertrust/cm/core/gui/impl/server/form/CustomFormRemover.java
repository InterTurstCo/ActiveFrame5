package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.gui.api.server.form.FormObjectsRemover;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * @author Denis Mitavskiy
 *         Date: 22.07.2014
 *         Time: 17:11
 */
@ComponentName("custom.form.remover")
public class CustomFormRemover implements FormObjectsRemover {
    @Override
    public void deleteForm(FormState currentFormState) {
        throw new GuiException("This remover will never delete anything");
    }
}
