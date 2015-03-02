package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.event.DeleteCollectionRowEvent;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeleteActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

import java.util.List;

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
            final DomainObjectSurferPlugin dosPlugin = (DomainObjectSurferPlugin) getPlugin();
            // вызываем событие удаления из коллекции
            dosPlugin.getLocalEventBus().fireEvent(new DeleteCollectionRowEvent(((DeleteActionData) result).getId()));
            // получаем конфигурацию для очистки формы
            final IsDomainObjectEditor editor = (IsDomainObjectEditor) plugin;
            final List<Id> selected = dosPlugin.getSelectedIds();
            final FormPluginConfig config;
            if (selected.isEmpty()) {
                final String domainObjectType = editor.getRootDomainObject().getTypeName();
                config = new FormPluginConfig(domainObjectType);
            } else {
                config = new FormPluginConfig(selected.get(0));
            }
            config.setPluginState(editor.getFormPluginState());
            config.setFormViewerConfig(editor.getFormViewerConfig());
            editor.replaceForm(config);
        }
    }

    @Override
    protected String getDefaultOnSuccessMessage() {
        return LocalizeUtil.get(BusinessUniverseConstants.ROW_IS_DELETED_MESSAGE);
    }
}

