package ru.intertrust.cm.core.gui.impl.server.action.system;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.UserSettingsFetcher;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.system.SplitterSettingsActionContext;
import ru.intertrust.cm.core.gui.model.util.UserSettingsHelper;

/**
 * @author Sergey.Okolot
 *         Created on 01.08.2014 17:00.
 */
@ComponentName(SplitterSettingsActionContext.COMPONENT_NAME)
public class SplitterSettingsActionHandler extends ActionHandler<SplitterSettingsActionContext, ActionData> {

    @Autowired private UserSettingsFetcher userSettingsFetcher;

    @Override
    public ActionData executeAction(SplitterSettingsActionContext context) {
        final DomainObject domainObject = userSettingsFetcher.getUserSettingsDomainObject(true);
        if(context.isCustomDrag()) {
            domainObject.setLong(UserSettingsHelper.DO_CUSTOM_SPLITTER_POSITION_FIELD_KEY, context.getPosition());
        }

        domainObject.setLong(UserSettingsHelper.DO_SPLITTER_POSITION_FIELD_KEY, context.getPosition());
        domainObject.setLong(UserSettingsHelper.DO_SPLITTER_ORIENTATION_FIELD_KEY, context.getOrientation());
        crudService.save(domainObject);
        return null;
    }

    @Override
    public SplitterSettingsActionContext getActionContext(final ActionConfig actionConfig) {
        return new SplitterSettingsActionContext();
    }
}
