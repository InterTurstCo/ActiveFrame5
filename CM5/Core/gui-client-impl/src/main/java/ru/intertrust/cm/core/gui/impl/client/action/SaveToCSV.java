package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SaveToCSVContext;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 11.01.14
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
@ComponentName("save-csv.action")
public class SaveToCSV extends SimpleServerAction {

    @Override
    public Component createNew() {
        return new SaveToCSV();
    }

    @Override
    public void execute() {
        super.execute();
    }

    @Override
    protected ActionContext appendCurrentContext(ActionContext initialContext) {

        SaveToCSVContext context = (SaveToCSVContext) initialContext;

        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        super.onSuccess(result);
    }
}