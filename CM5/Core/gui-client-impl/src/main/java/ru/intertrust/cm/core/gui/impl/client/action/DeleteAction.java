package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.event.DeleteCollectionRowEvent;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeleteActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author IPetrov
 *         Date: 06.12.13
 *         Time: 18:08
 */
@ComponentName("delete.action")
public class DeleteAction extends SimpleServerAction {

    @Override
    public Component createNew() {
        return new DeleteAction();
    }

    @Override
    protected SaveActionContext appendCurrentContext(ActionContext initialContext) {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        final FormState formState = editor.getFormState();
        final SaveActionContext context = (SaveActionContext) initialContext;
        context.setRootObjectId(formState.getObjects().getRootNode().getDomainObject().getId());
        context.setFormState(formState);
        context.setPluginState(editor.getFormPluginState());
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        if (result instanceof DeleteActionData) {
            // вызываем событие удаления из коллекции
            plugin.getLocalEventBus().fireEvent(new DeleteCollectionRowEvent(((DeleteActionData) result).getId(), plugin));

        }
    }

    @Override
    protected String getDefaultOnSuccessMessage() {
        return LocalizeUtil.get(LocalizationKeys.ROW_IS_DELETED_MESSAGE_KEY, BusinessUniverseConstants.ROW_IS_DELETED_MESSAGE);
    }
}

