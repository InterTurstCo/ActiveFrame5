package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.UserSettingsFetcher;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.InitialNavigationLinkContext;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.06.2015
 *         Time: 8:51
 */
@ComponentName(InitialNavigationLinkContext.COMPONENT_NAME)
public class InitialNavigationLinkActionHandler extends ActionHandler<InitialNavigationLinkContext, ActionData> {

    @Autowired
    private UserSettingsFetcher userSettingsFetcher;

    @Override
    public ActionData executeAction(InitialNavigationLinkContext context) {
        DomainObject domainObject = userSettingsFetcher.getUserSettingsDomainObject(true);
        domainObject.setString(UserSettingsHelper.DO_INITIAL_NAVIGATION_LINK_KEY, context.getInitialNavigationLink());
        crudService.save(domainObject);
        return null;
    }

    @Override
    public InitialNavigationLinkContext getActionContext(final ActionConfig actionConfig) {
        return new InitialNavigationLinkContext(actionConfig);
    }
}