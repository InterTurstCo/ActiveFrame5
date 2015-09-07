package ru.intertrust.cm.core.gui.impl.client.action.system;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.InitialNavigationLinkActionContext;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.2015
 *         Time: 8:41
 */
@ComponentName(InitialNavigationLinkActionContext.COMPONENT_NAME)
public class InitialNavigationLinkAction extends AbstractUserSettingAction {
    @Override
    public Component createNew() {
        return new InitialNavigationLinkAction();
    }
}
