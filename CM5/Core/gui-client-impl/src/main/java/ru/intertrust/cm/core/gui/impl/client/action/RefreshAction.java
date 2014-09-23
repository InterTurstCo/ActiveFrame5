package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.plugins.objectsurfer.DomainObjectSurferPlugin;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.RefreshActionContext;
import ru.intertrust.cm.core.gui.model.action.RefreshActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Sergey.Okolot
 *         Created on 09.06.2014 13:37.
 */
@ComponentName("refresh.action")
public class RefreshAction extends SimpleServerAction {

    @Override
    public Component createNew() {
        return new RefreshAction();
    }

    @Override
    protected RefreshActionContext appendCurrentContext(ActionContext initialContext) {
        final RefreshActionContext context = getInitialContext();
        DomainObjectSurferPlugin plugin = (DomainObjectSurferPlugin) getPlugin();
        CollectionRowsRequest request = plugin.getCollectionPlugin().getCollectionRowRequest();
        context.setRequest(request);
        IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        FormState formState = editor.getFormState();
        Id id = formState.getObjects().getRootNode().getDomainObject().getId();
        context.setId(id);
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        RefreshActionData refreshResult = (RefreshActionData) result;
        DomainObjectSurferPlugin plugin = (DomainObjectSurferPlugin) getPlugin();
        plugin.getCollectionPlugin().refreshCollection(refreshResult.getResponse().getCollectionRows());
    }

    @Override
    protected String getDefaultOnSuccessMessage() {
        return "Коллекция успешно обновлена";
    }
}
