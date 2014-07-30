package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Sergey.Okolot
 */
@ComponentName("close.in.central.panel.action")
public class CloseInCentralPanelAction extends Action {

    @Override
    protected void execute() {
        plugin.getOwner().asWidget().setWidth("100%");
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final Id objectId = editor.getRootDomainObject().getId();
        Plugin parent = plugin.getOwner().getParentPlugin(plugin);
        if (objectId != null && parent == null  &&  plugin.getLocalEventBus()!= null) {
            plugin.getLocalEventBus().fireEvent(new CollectionRowSelectedEvent(objectId));
        }
        if (parent instanceof DomainObjectSurferPlugin) {
            IsDomainObjectEditor parentEditor = (IsDomainObjectEditor) parent;
            DomainObject domainObject = parentEditor.getRootDomainObject( );
            if (domainObject != null && domainObject.getId() != null) {
                Id parentId = domainObject.getId();
                if (plugin.getLocalEventBus() != null) {
                    plugin.getLocalEventBus().fireEvent(new CollectionRowSelectedEvent(parentId));
                }
            }
        }
        plugin.getOwner().closeCurrentPlugin();
    }

    @Override
    public Component createNew() {
        return new CloseInCentralPanelAction();
    }

}
