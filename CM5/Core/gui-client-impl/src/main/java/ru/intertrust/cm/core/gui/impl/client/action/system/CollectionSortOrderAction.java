package ru.intertrust.cm.core.gui.impl.client.action.system;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.CollectionSortOrderActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 01.08.2014 15:09.
 */
@ComponentName(CollectionSortOrderActionContext.COMPONENT_NAME)
public class CollectionSortOrderAction extends AbstractUserSettingAction {

    @Override
    public Component createNew() {
        return new CollectionSortOrderAction();
    }
}