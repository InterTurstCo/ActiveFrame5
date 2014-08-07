package ru.intertrust.cm.core.gui.impl.client.action.system;

import com.google.gwt.user.client.Window;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.ThemeActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 07.08.2014 17:12.
 */
@ComponentName(ThemeActionContext.COMPONENT_NAME)
public class ThemeAction extends AbstractUserSettingAction {

    @Override
    public Component createNew() {
        return new ThemeAction();
    }

    @Override
    protected void onSuccessHandler(Dto result) {
        Window.Location.reload();
    }
}
