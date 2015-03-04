package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.LanguageActionContext;

/**
 * @author Lesia Puhova
 *         Date: 04.03.2015
 *         Time: 13:25
 */
@ComponentName(LanguageActionContext.COMPONENT_NAME)
public class LanguageActionHandler extends ActionHandler<LanguageActionContext, ActionData> {

    @Autowired
    ProfileService profileService;


    @Override
    public ActionData executeAction(LanguageActionContext context) {
        profileService.setPersonLocale(context.getLocale());

        return null;
    }

    @Override
    public LanguageActionContext getActionContext(final ActionConfig actionConfig) {
        return new LanguageActionContext(actionConfig);
    }
}
