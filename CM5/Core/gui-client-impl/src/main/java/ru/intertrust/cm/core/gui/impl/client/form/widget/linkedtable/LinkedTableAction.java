package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.action.LinkedTableActionRequest;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

/**
 * Created by andrey on 18.12.14.
 */
public abstract class LinkedTableAction implements Component {
    private PostPerformCallback callback;
    protected FormState rowFormState;

    void perform(final Id objectId, final int rowIndex) {
        if (getServerComponentName() == null) {
            performClientAction(objectId, rowIndex);
        } else {
            performServerClientAction(objectId, rowIndex);
        }

    }

    private void performServerClientAction(final Id objectId, final int rowIndex) {
        LinkedTableActionRequest request = new LinkedTableActionRequest();
        request.setObjectId(objectId);
        Command command = new Command("execute", getServerComponentName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                performClientAction(objectId, rowIndex);
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining hyperlink", caught);
            }
        });
    }

    private void performClientAction(Id objectId, int rowIndex) {
        execute(objectId, rowIndex);
        if (callback != null) {
            callback.onPerform();
        }
    }

    protected abstract void execute(Id id, int rowIndex);

    protected abstract String getServerComponentName();

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
