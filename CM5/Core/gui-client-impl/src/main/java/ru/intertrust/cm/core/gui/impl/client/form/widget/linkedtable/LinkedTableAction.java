package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionRequest;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionResponse;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * Created by andrey on 18.12.14.
 */
public abstract class LinkedTableAction implements Component {
    private PostPerformCallback callback;
    protected FormState rowFormState;

    void perform(final Id objectId, final int rowIndex, String accessChecker, String newObjectsAccessChecker) {
        LinkedTableActionRequest request = new LinkedTableActionRequest();
        request.setAccessCheckerComponent(accessChecker);
        request.setObjectId(objectId);
        request.setRowIndex(rowIndex);
        request.setNewObjectsAccessCheckerComponent(newObjectsAccessChecker);
        Command command = new Command("execute", getServerComponentName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                LinkedTableActionResponse response = (LinkedTableActionResponse) result;
                if (response.isAccessGranted()) {
                    execute(objectId, rowIndex);
                    if (callback != null) {
                        callback.onPerform();
                    }
                } else {
                    Window.alert("operation is not granted");
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink", caught);
            }
        });

    }

    abstract void execute(Id id, int rowIndex);

    abstract String getServerComponentName();

    public void setCallback(PostPerformCallback callback) {
        this.callback = callback;
    }

    public PostPerformCallback getCallback() {
        return callback;
    }

    public void setRowFormState(FormState rowFormState) {
        this.rowFormState = rowFormState;
    }
}
