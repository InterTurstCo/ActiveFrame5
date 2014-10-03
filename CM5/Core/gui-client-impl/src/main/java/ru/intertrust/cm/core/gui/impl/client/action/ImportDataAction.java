package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.ImportDataActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.IsDomainObjectEditor;

/**
 * 
 * @author lyakin
 *
 */
@ComponentName("import.data.action")
public class ImportDataAction extends SimpleServerAction {

    @Override
    protected void onSuccess(ActionData result) {
        
    }

    @Override
    protected ActionContext appendCurrentContext(ActionContext initialContext) {
        ImportDataActionContext context = getInitialContext();
        final IsDomainObjectEditor editor = (IsDomainObjectEditor) getPlugin();
        FormState formState = editor.getFormState();
        context.setFormState(formState);
        return context;  
    }

    @Override
    public Component createNew() {
        return new ImportDataAction();
    }

}
