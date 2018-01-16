package ru.intertrust.cm.core.gui.impl.server.action.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.ResetPluginSettingsActionContext;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.*;
import javax.interceptor.Interceptors;

/**
 * @author Sergey.Okolot
 *         Created on 06.08.2014 16:44.
 */

@ComponentName(ResetPluginSettingsActionContext.COMPONENT_NAME)
public class ResetPluginSettingsActionHandler extends ActionHandler<ResetPluginSettingsActionContext, ActionData>
        {

    @Autowired
    private CrudService crudService;
    @Autowired
    private CollectionsService collectionsService;
    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @EJB
    SettingsUtil settingsUtil;


    @Override
    public ActionData executeAction(ResetPluginSettingsActionContext context) {
        final List<Filter> filters = new ArrayList<>();
        filters.add(Filter.create("byLink", 0, new StringValue(context.getLink())));
        filters.add(Filter.create("byPerson", 0, new StringValue(currentUserAccessor.getCurrentUser())));
        final IdentifiableObjectCollection collection =
                collectionsService.findCollection("bu_nav_link_collections", null, filters);

        if (collection.size() > 0) {
            final List<Id> ids = new ArrayList<>();
            for (Iterator<IdentifiableObject> it = collection.iterator(); it.hasNext(); ) {
                ids.add(it.next().getId());
            }
            settingsUtil.deleteIds(ids);
        }
        return null;
    }

    @Override
    public ResetPluginSettingsActionContext getActionContext(final ActionConfig actionConfig) {
        return new ResetPluginSettingsActionContext();
    }
}
