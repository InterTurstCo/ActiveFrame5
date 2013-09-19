package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 18:11
 */
public abstract class SimpleServerAction extends Action {
    public void execute() {
        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                Window.alert(result.toString());
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Oops");
            }
        };
        Command command = new Command("executeAction", this.getName(), getContext());
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, callback);
    }

    protected ActionContext getContext() {
        return null;
    }

    protected void onSuccess(ActionData result) {

    }
}
