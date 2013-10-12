package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * @author Denis Mitavskiy
 *         Date: 22.09.13
 *         Time: 23:09
 */
public class SaveActionContext extends ActionContext {
    private FormState formState;

    public FormState getFormState() {
        return formState;
    }

    public void setFormState(FormState formState) {
        this.formState = formState;
    }
}
