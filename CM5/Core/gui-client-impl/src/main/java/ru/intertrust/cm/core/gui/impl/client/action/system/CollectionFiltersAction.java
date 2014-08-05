package ru.intertrust.cm.core.gui.impl.client.action.system;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.CollectionFiltersActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 04.08.2014 14:55.
 */
@ComponentName(CollectionFiltersActionContext.COMPONENT_NAME)
public class CollectionFiltersAction extends AbstractUserSettingAction {

    @Override
    public Component createNew() {
        return new CollectionFiltersAction();
    }
}
