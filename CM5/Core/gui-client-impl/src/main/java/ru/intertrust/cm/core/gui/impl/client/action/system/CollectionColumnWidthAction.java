package ru.intertrust.cm.core.gui.impl.client.action.system;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnWidthActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 28.07.2014 15:17.
 */
@ComponentName(CollectionColumnWidthActionContext.COMPONENT_NAME)
public class CollectionColumnWidthAction extends AbstractUserSettingAction {

    @Override
    public Component createNew() {
        return new CollectionColumnWidthAction();
    }
}
