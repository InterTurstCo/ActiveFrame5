package ru.intertrust.cm.core.gui.impl.client.action.configextension;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by Ravil on 24.05.2017.
 */
@ComponentName("validate.extensions.action.handler")
public class ValidateDraftsAction extends ApplyDraftsAction {

    @Override
    public Component createNew() {
        return new ValidateDraftsAction();
    }
}
