package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.gui.api.server.form.FormBeforeSaveInterceptor;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;

/**
 * @author Denis Mitavskiy
 *         Date: 14.10.2014
 *         Time: 15:18
 */
@ComponentName("a1.before.save.handler")
public class A1FormBeforeSaveInterceptor implements FormBeforeSaveInterceptor {
    @Override
    public void beforeSave(FormState formState) {
        ((TextState) formState.getWidgetState("b_name1")).setText("B1 NAME1 - ALWAYS");
    }
}
