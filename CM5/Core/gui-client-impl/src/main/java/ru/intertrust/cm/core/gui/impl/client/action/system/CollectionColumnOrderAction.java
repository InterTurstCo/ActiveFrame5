package ru.intertrust.cm.core.gui.impl.client.action.system;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnOrderActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 31.07.2014 17:26.
 */
@ComponentName(CollectionColumnOrderActionContext.COMPONENT_NAME)
public class CollectionColumnOrderAction extends AbstractUserSettingAction {

    @Override
    public Component createNew() {
        return new CollectionColumnOrderAction();
    }

}
