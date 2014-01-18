package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Sergey.Okolot
 */
@ComponentName("toggle.edit.off.action")
public class ToggleEditOffAction extends Action {

    @Override
    public void execute() {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final Id objectId = editor.getRootDomainObject().getId();
        if (objectId != null) {
            plugin.getLocalEventBus().fireEvent(new CollectionRowSelectedEvent(objectId));
        }
        plugin.getOwner().closeCurrentPlugin();
    }

    @Override
    public Component createNew() {
        return new ToggleEditOffAction();
    }
}
