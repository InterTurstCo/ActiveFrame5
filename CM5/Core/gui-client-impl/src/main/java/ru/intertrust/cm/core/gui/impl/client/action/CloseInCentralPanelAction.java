package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.CollectionRowSelectedEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.DomainObjectSource;
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
        DomainObjectSource source = editor.getFormPluginState() == null ? DomainObjectSource.COLLECTION
                : editor.getFormPluginState().getDomainObjectSource();
        if (objectId != null && plugin.getLocalEventBus()!= null && DomainObjectSource.COLLECTION.equals(source)) {
            plugin.getLocalEventBus().fireEvent(new CollectionRowSelectedEvent(objectId));
        }
        PluginPanel owner = plugin.getOwner();
        owner.closeCurrentPlugin();
        if(DomainObjectSource.HYPERLINK.equals(source)){
            owner.getCurrentPlugin().refresh();
        }
    }

    @Override
    public Component createNew() {
        return new CloseInCentralPanelAction();
    }

}
