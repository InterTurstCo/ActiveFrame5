package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * @author Sergey.Okolot
 *         Created on 22.09.2014 13:21.
 */
@ComponentName("simple.action")
public class SimpleAction extends SimpleServerAction {

    @Override
    protected ActionContext appendCurrentContext(ActionContext initialContext) {
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        initialContext.setRootObjectId(editor.getRootDomainObject().getId());
        return initialContext;
    }

    private FormPluginData DomainObject(FormPluginData initialData) {
        return null;
    }

    @Override
    protected void onSuccess(ActionData result) {
        Window.alert(result.getOnSuccessMessage() + '\n' + result.getOnErrorMessage());
    }

    @Override
    public Component createNew() {
        return new SimpleAction();
    }
}
