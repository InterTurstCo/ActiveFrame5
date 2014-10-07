package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Sergey.Okolot
 *         Created on 22.09.2014 13:21.
 */
@ComponentName(SimpleActionContext.COMPONENT_NAME)
public class SimpleAction extends SimpleServerAction {

    @Override
    protected ActionContext appendCurrentContext(ActionContext initialContext) {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        initialContext.setRootObjectId(editor.getRootDomainObject().getId());
        ((SimpleActionContext) initialContext).setMainFormState(editor.getFormState());
        return initialContext;
    }

    @Override
    protected void onSuccess(ActionData result) {
        FormPluginData formPluginData = ((SimpleActionData) result).getPluginData();
        Plugin plugin = getPlugin();
        if (plugin.getLocalEventBus() != null) {
            ((IsDomainObjectEditor) plugin).setFormState(formPluginData.getFormDisplayData().getFormState());
            plugin.setToolbarContext(formPluginData.getToolbarContext());
            // вызываем событие обновления коллекции
            plugin.getLocalEventBus().fireEvent(new UpdateCollectionEvent(
                    formPluginData.getFormDisplayData().getFormState().getObjects().getRootNode().getDomainObject()));
        }
    }

    @Override
    public Component createNew() {
        return new SimpleAction();
    }
}
