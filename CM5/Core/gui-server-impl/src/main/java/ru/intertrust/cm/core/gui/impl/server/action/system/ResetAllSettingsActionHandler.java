package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.ResetAllSettingsActionContext;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey.Okolot
 *         Created on 06.08.2014 16:40.
 */
@ComponentName(ResetAllSettingsActionContext.COMPONENT_NAME)
public class ResetAllSettingsActionHandler extends ActionHandler<ResetAllSettingsActionContext, ActionData> {

    @Autowired private CollectionsService collectionsService;

    @EJB
    SettingsUtil settingsUtil;

    @Override
    public ActionData executeAction(ResetAllSettingsActionContext context) {
        final List<Id> ids = new ArrayList<>();
        final List<Filter> filters = new ArrayList<>();
        filters.add(Filter.create("byPerson", 0, new StringValue(currentUserAccessor.getCurrentUser())));
        IdentifiableObjectCollection collection =
                collectionsService.findCollection("bu_nav_link_collections", null, filters);
        for (IdentifiableObject iobj : collection) {
            ids.add(iobj.getId());
        }
        collection = collectionsService.findCollection("bu_user_settings_collection", null, filters);
        for (IdentifiableObject iobj : collection) {
            ids.add(iobj.getId());
        }
        if (!ids.isEmpty()) {
            settingsUtil.deleteIds(ids);
        }

        settingsUtil.saveTheme(context.getDefaultTheme());
        return null;
    }

    @Override
    public ResetAllSettingsActionContext getActionContext(final ActionConfig actionConfig) {
        return new ResetAllSettingsActionContext();
    }
}
