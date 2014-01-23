package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 18:11
 */
public abstract class SimpleServerAction extends Action {
    private List<ActionSuccessListener> successListeners = new ArrayList<ActionSuccessListener>();

    public void execute() {
        AsyncCallback<Dto> callback = new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                SimpleServerAction.this.onSuccess((ActionData) result);
                for (ActionSuccessListener listener : successListeners) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof GuiException) {
                    SimpleServerAction.this.onFailure((GuiException) caught);
                } else {
                    Window.alert("System exception");
                }
            }
        };
        try {
            ActionContext currentContext = appendCurrentContext(initialContext);
            Command command = new Command("executeAction", this.getName(), currentContext);
            BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, callback);
        } catch (GuiException e) {
            Window.alert(e.getMessage());
        }
    }

    protected ActionContext appendCurrentContext(ActionContext initialContext) {
        return initialContext;
    }

    protected void onSuccess(ActionData result) {
        Window.alert("Success");
    }

    protected void onFailure(GuiException exception) {
        Window.alert(exception.getMessage());
    }

    public void addActionSuccessListener(ActionSuccessListener listener) {
        successListeners.add(listener);
    }
}
