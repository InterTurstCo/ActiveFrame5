package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Sergey.Okolot
 *         Created on 09.06.2014 13:37.
 */
@ComponentName("refresh.action")
public class RefreshAction extends Action {

    @Override
    protected void execute() {
        getPlugin().refresh();
    }

    @Override
    public Component createNew() {
        return new RefreshAction();
    }
//
//    @Override
//    protected RefreshActionContext appendCurrentContext(ActionContext initialContext) {
//        final RefreshActionContext context = getInitialContext();
//        DomainObjectSurferPlugin plugin = (DomainObjectSurferPlugin) getPlugin();
//        CollectionRowsRequest request = plugin.getCollectionPlugin().getCollectionRowRequest();
//        context.setRequest(request);
//        IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
//        FormState formState = editor.getFormState();
//        Id id = formState.getObjects().getRootNode().getDomainObject().getId();
//        context.setId(id);
//        return context;
//    }
//
//    @Override
//    protected void onSuccess(ActionData result) {
//        RefreshActionData refreshResult = (RefreshActionData) result;
//        DomainObjectSurferPlugin plugin = (DomainObjectSurferPlugin) getPlugin();
//        plugin.getCollectionPlugin().refreshCollection(refreshResult.getResponse().getCollectionRows());
//    }
//
//    @Override
//    protected String getDefaultOnSuccessMessage() {
//        return "Коллекция успешно обновлена";
//    }
}
