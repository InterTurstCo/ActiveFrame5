package ru.intertrust.cm.core.gui.impl.client.action.system;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.LanguageActionContext;

/**
 * @author Lesia Puhova
 *         Date: 04.03.2015
 *         Time: 15:34
 */
@ComponentName(LanguageActionContext.COMPONENT_NAME)
public class LanguageAction extends AbstractUserSettingAction {

    @Override
    public Component createNew() {
        return new LanguageAction();
    }

    @Override
    protected void onSuccessHandler(Dto result) {
        Window.Location.reload();
    }
}
