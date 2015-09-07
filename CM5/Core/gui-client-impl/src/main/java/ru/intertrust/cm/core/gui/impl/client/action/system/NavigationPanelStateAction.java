package ru.intertrust.cm.core.gui.impl.client.action.system;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.NavigationPanelStateActionContext;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.07.2015
 *         Time: 22:28
 */
@ComponentName(NavigationPanelStateActionContext.COMPONENT_NAME)
public class NavigationPanelStateAction extends AbstractUserSettingAction {
    @Override
    public Component createNew() {
        return new NavigationPanelStateAction();
    }
}
