package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Denis Mitavskiy
 *         Date: 18.09.13
 *         Time: 22:00
 */
@ComponentName("save.action")
public class SaveAction extends SimpleServerAction {

    @Override
    public void execute() {
        super.execute();
    }

    @Override
    public Component createNew() {
        return new SaveAction();
    }

    @Override
    protected SaveActionContext appendCurrentContext(ActionContext initialContext) {
        FormState formState = ((IsDomainObjectEditor) getPlugin()).getFormState();
        SaveActionContext context = (SaveActionContext) initialContext;
        context.setRootObjectId(formState.getObjects().getRootNode().getDomainObject().getId());
        context.setFormState(formState);
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        FormPluginData formPluginData = ((SaveActionData) result).getFormPluginData();
        Plugin plugin = getPlugin();
        ((IsDomainObjectEditor) plugin).setFormState(formPluginData.getFormDisplayData().getFormState());
        plugin.setActionContexts(formPluginData.getActionContexts());

        // вызываем событие обновления коллекции
        ((DomainObjectSurferPlugin) plugin).getEventBus().fireEvent(new UpdateCollectionEvent(
                formPluginData.getFormDisplayData().getFormState().getObjects().getRootNode().getDomainObject()));

        Window.alert("Saved!!!");
    }
}

