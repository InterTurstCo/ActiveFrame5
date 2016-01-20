package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.UserSettingsFetcher;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.InitialNavigationLinkActionContext;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.2015
 *         Time: 8:51
 */
@ComponentName(InitialNavigationLinkActionContext.COMPONENT_NAME)
public class InitialNavigationLinkActionHandler extends ActionHandler<InitialNavigationLinkActionContext, ActionData> {

    @Autowired
    private UserSettingsFetcher userSettingsFetcher;

    @Override
    public ActionData executeAction(InitialNavigationLinkActionContext context) {
        DomainObject domainObject = userSettingsFetcher.getUserSettingsDomainObject(true);
        domainObject.setString(UserSettingsHelper.DO_INITIAL_NAVIGATION_LINK_KEY, context.getInitialNavigationLink());
        domainObject.setString(UserSettingsHelper.DO_INITIAL_APPLICATION_NAME, context.getApplication());
        crudService.save(domainObject);
        return null;
    }

    @Override
    public InitialNavigationLinkActionContext getActionContext(final ActionConfig actionConfig) {
        return new InitialNavigationLinkActionContext(actionConfig);
    }
}