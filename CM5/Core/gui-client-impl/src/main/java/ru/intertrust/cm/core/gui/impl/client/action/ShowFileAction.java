package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.ShowFileActionData;

@ComponentName("show.file.action")
public class ShowFileAction extends SimpleAction{

    @Override
    public Component createNew() {
        return new ShowFileAction();
    }

    @Override
    protected void onSuccess(ActionData result) {
        ShowFileActionData viewFileActionData = (ShowFileActionData)result;

        //view-file/{unid}/{inline}/{filename}
        String query = com.google.gwt.core.client.GWT.getHostPageBaseURL() +
                "af5-services/view-file/" + viewFileActionData.getFileUnid() +
                "/" + viewFileActionData.isInline() +
                "/" + viewFileActionData.getFileName();
        Window.open(com.google.gwt.http.client.URL.encode(query), viewFileActionData.getFileName(), "");
    }
}
