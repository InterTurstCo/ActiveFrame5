package ru.intertrust.cm.core.gui.impl.client.action.system;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.system.CollectionColumnHiddenActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 06.08.2014 11:56.
 */
@ComponentName(CollectionColumnHiddenActionContext.COMPONENT_NAME)
public class CollectionColumnHiddenAction extends AbstractUserSettingAction {
    @Override
    public Component createNew() {
        return new CollectionColumnHiddenAction();
    }
}
