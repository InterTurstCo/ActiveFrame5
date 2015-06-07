package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.UserSettingsFetcher;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.ThemeActionContext;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

/**
 * @author Sergey.Okolot
 *         Created on 07.08.2014 17:08.
 */
@ComponentName(ThemeActionContext.COMPONENT_NAME)
public class ThemeActionHandler extends ActionHandler<ThemeActionContext, ActionData> {

    @Autowired private UserSettingsFetcher userSettingsFetcher;

    @Override
    public ActionData executeAction(ThemeActionContext context) {
        final DomainObject domainObject = userSettingsFetcher.getUserSettingsDomainObject();
        domainObject.setString(UserSettingsHelper.DO_THEME_FIELD_KEY, context.getThemeName());
        crudService.save(domainObject);
        return null;
    }

    @Override
    public ThemeActionContext getActionContext(final ActionConfig actionConfig) {
        return new ThemeActionContext(actionConfig);
    }
}
