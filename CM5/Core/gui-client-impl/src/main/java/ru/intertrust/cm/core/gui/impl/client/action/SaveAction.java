package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

/**
 * @author Denis Mitavskiy
 *         Date: 18.09.13
 *         Time: 22:00
 */
@ComponentName("save.action")
public class SaveAction extends SimpleServerAction {

    @Override
    public Component createNew() {
        return new SaveAction();
    }

    @Override
    protected SaveActionContext appendCurrentContext(ActionContext initialContext) {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        FormState formState = editor.getFormState();
        SaveActionContext context = (SaveActionContext) initialContext;
        context.setRootObjectId(formState.getObjects().getRootNode().getDomainObject().getId());
        context.setFormState(formState);
        context.setPluginState(editor. getFormPluginState());
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        FormPluginData formPluginData = ((SaveActionData) result).getFormPluginData();
        Plugin plugin = getPlugin();
        if (plugin.getLocalEventBus() != null) {
            ((IsDomainObjectEditor) plugin).setFormState(formPluginData.getFormDisplayData().getFormState());
            plugin.setActionContexts(formPluginData.getActionContexts());
            // вызываем событие обновления коллекции
            plugin.getLocalEventBus().fireEvent(new UpdateCollectionEvent(
                    formPluginData.getFormDisplayData().getFormState().getObjects().getRootNode().getDomainObject()));
        }

        Window.alert("Saved!!!");
    }

    @Override
    public boolean isValid() {
        if (this.getPlugin() instanceof  FormPlugin) {
            FormPlugin plugin = (FormPlugin)this.getPlugin();
            PluginView view = plugin.getView();
            FormPanel panel = (FormPanel)view.getViewWidget();
            ValidationResult validationResult = new ValidationResult();
            for (BaseWidget widget : panel.getWidgets()) {
                validationResult.append(widget.validate());
            }
            if (validationResult.hasErrors()) {
                Window.alert("Please correct validation errors before saving");
                return false;
            }
        }
        return true;
    }
}

