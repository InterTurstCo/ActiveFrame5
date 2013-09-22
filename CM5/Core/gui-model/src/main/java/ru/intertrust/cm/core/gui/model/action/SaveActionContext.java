package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.gui.model.form.Form;

/**
 * @author Denis Mitavskiy
 *         Date: 22.09.13
 *         Time: 23:09
 */
public class SaveActionContext extends ActionContext {
    private Form form;

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }
}
