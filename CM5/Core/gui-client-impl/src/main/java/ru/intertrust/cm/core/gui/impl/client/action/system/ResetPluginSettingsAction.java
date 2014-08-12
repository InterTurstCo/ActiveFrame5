package ru.intertrust.cm.core.gui.impl.client.action.system;

import com.google.gwt.user.client.Window;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.ResetPluginSettingsActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 06.08.2014 16:38.
 */
@ComponentName(ResetPluginSettingsActionContext.COMPONENT_NAME)
public class ResetPluginSettingsAction extends AbstractUserSettingAction {

    @Override
    public Component createNew() {
        return new ResetPluginSettingsAction();
    }

    protected void onSuccessHandler(Dto result) {
        Window.Location.reload();
    }
}
