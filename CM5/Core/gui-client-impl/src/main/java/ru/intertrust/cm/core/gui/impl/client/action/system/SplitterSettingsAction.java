package ru.intertrust.cm.core.gui.impl.client.action.system;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.SplitterSettingsActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 01.08.2014 16:47.
 */
@ComponentName(SplitterSettingsActionContext.COMPONENT_NAME)
public class SplitterSettingsAction extends AbstractUserSettingAction {

    @Override
    public Component createNew() {
        return new SplitterSettingsAction();
    }
}
