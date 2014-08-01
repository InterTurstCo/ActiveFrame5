package ru.intertrust.cm.core.gui.impl.client.action.system;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * @author Sergey.Okolot
 *         Created on 31.07.2014 17:28.
 */
public abstract class AbstractUserSettingAction extends Action {

    @Override
    protected void execute() {
        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {

            @Override
            public void onSuccess(Dto result) {
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Невозможно сохранить настройки пользователя");
            }
        };
        Command command = new Command("executeAction", this.getName(), getInitialContext());
        BusinessUniverseServiceAsync.Impl.executeCommand(command, callback);
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
