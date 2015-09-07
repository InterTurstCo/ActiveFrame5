package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.UserSettingsFetcher;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.InitialNavigationLinkActionContext;
import ru.intertrust.cm.core.gui.model.action.system.NavigationPanelStateActionContext;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.07.2015
 *         Time: 22:18
 */
@ComponentName(NavigationPanelStateActionContext.COMPONENT_NAME)
public class NavigationPanelStateActionHandler  extends ActionHandler<NavigationPanelStateActionContext, ActionData> {

    @Autowired
    private UserSettingsFetcher userSettingsFetcher;

    @Override
    public ActionData executeAction(NavigationPanelStateActionContext context) {
        DomainObject domainObject = userSettingsFetcher.getUserSettingsDomainObject(true);
        domainObject.setBoolean(UserSettingsHelper.DO_NAVIGATION_PANEL_SECOND_LEVEL_PINNED_KEY, context.isPinned());
        crudService.save(domainObject);
        return null;
    }

    @Override
    public InitialNavigationLinkActionContext getActionContext(final ActionConfig actionConfig) {
        return new InitialNavigationLinkActionContext(actionConfig);
    }
}
