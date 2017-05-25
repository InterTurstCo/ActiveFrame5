package ru.intertrust.cm.core.gui.impl.client.action.configextension;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by Ravil on 25.05.2017.
 */
@ComponentName("activate.extensions.action.handler")
public class ActivateConfigAction extends ApplyDraftsAction {
    @Override
    public Component createNew() {
        return new ActivateConfigAction();
    }
}
