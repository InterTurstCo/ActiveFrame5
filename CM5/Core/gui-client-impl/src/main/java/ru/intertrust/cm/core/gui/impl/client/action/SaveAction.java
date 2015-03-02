package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.UpdateCollectionEvent;
import ru.intertrust.cm.core.gui.impl.client.form.FormPanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
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
@Deprecated
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
        context.setPluginState(editor.getFormPluginState());
        context.setFormViewerConfig(editor.getFormViewerConfig());
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        FormPluginData formPluginData = ((SaveActionData) result).getFormPluginData();
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        IdentifiableObject root = formPluginData.getFormDisplayData().getFormState().getObjects().getRootNode().getDomainObject();
        if (plugin.getLocalEventBus() != null) {
            editor.setFormState(formPluginData.getFormDisplayData().getFormState());
            editor.setFormToolbarContext(formPluginData.getToolbarContext());
            plugin.getView().updateActionToolBar();
            plugin.getLocalEventBus().fireEvent(new UpdateCollectionEvent(root));
        }
    }

    @Override
    protected String getDefaultOnSuccessMessage() {
        return LocalizeUtil.get(BusinessUniverseConstants.SAVED_MESSAGE);
    }

    @Override
    public boolean isValid() {
        Plugin plugin = null;
        if (getPlugin() instanceof FormPlugin) {
            plugin = this.getPlugin();
        } else if (getPlugin() instanceof DomainObjectSurferPlugin) {
            plugin = ((DomainObjectSurferPlugin) getPlugin()).getFormPlugin();
        }
        if (plugin != null) {
            PluginView view = plugin.getView();
            FormPanel panel = (FormPanel) view.getViewWidget();
            ValidationResult validationResult = new ValidationResult();
            for (BaseWidget widget : panel.getWidgets()) {
                if (widget.isEditable()) {
                    validationResult.append(widget.validate());
                }
            }
            if (validationResult.hasErrors()) {
                ApplicationWindow.errorAlert(LocalizeUtil.get(BusinessUniverseConstants
                        .CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE));
                return false;
            }
        }
        return true;
    }
}

