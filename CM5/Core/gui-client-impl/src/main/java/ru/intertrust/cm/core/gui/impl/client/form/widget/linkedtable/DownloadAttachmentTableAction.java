package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.DownloadAttachmentActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.Collection;

/**
 * @author Lesia Puhova
 *         Date: 23.12.2014
 *         Time: 16:51
 *
 * Пример реализации действия, используемого в linked-domain-objects-table, которое получает id и временное имя вложения
 * и вызывает действие скачивания вложения.
 * Этот класс нужен в первую очередь для тестирования действия скачивания вложений. В реальном приложении, вероятно,
 * оно будет заменено другим действием, логика которого соответствует конкретным требованиям.
 */
@ComponentName("download.attachment.table.action")
public class DownloadAttachmentTableAction extends LinkedTableAction {


    @Override
    protected void execute(Id parentObjectId, int rowIndex) {
        Command command = new Command("getAttachmentId", "download.attachment.action", parentObjectId);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                DownloadAttachmentActionContext context = new DownloadAttachmentActionContext();
                context.setId((Id)result);
                context.setTempName(getTemporaryName());
                final Action action = ComponentRegistry.instance.get("download.attachment.action");
                action.setInitialContext(context);
                action.perform();
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining attachment Id", caught);
            }
        });

    }

    @Override
    protected String getServerComponentName() {
        return "download.attachment.table.action";

    }

    private String getTemporaryName() {
        if (rowFormState !=null) {
            Collection<WidgetState> widgetStates = rowFormState.getFullWidgetsState().values();
            for (WidgetState state : widgetStates) {
                if (state instanceof AttachmentBoxState) {
                    AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
                    for (AttachmentItem item : attachmentBoxState.getAttachments()) {
                        if (item.getTemporaryName() != null) {
                            return item.getTemporaryName(); //первый попавшийся
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "download.attachment.action";
    }

    @Override
    public Component createNew() {
        return new DownloadAttachmentTableAction();
    }
}
