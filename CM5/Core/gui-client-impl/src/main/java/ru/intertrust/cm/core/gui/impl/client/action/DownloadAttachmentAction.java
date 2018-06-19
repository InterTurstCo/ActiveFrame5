package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.AttachmentAvailabilityActionContext;
import ru.intertrust.cm.core.gui.model.action.DownloadAttachmentActionContext;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import static ru.intertrust.cm.core.business.api.dto.util.ModelConstants.*;

/**
 * @author Lesia Puhova
 *         Date: 23.12.2014
 *         Time: 16:51
 *         <p/>
 *         Действие скачивания вложения
 */
@ComponentName("download.attachment.action")
public class DownloadAttachmentAction extends Action {

    @Override
    public Component createNew() {
        return new DownloadAttachmentAction();
    }

    @Override
    protected void execute() {
        StringBuilder url = new StringBuilder(com.google.gwt.core.client.GWT.getHostPageBaseURL())
                .append("attachment-download?");
        if (getTemporaryName() != null) {
            url.append(DOWNLOAD_TEMP_NAME).append(DOWNLOAD_EQUAL).append(getTemporaryName());
        } else if (getId() != null) {
            url.append(DOWNLOAD_ID).append(DOWNLOAD_EQUAL).append(getId().toStringRepresentation());
        }

        if (getId() != null || getTemporaryName() != null) {
            tryDownloadAttachment(url.toString());

        } else {
            alertError();
        }
    }

    private Id getId() {
        DownloadAttachmentActionContext context = getInitialContext();
        return context.getId();
    }

    private String getTemporaryName() {
        DownloadAttachmentActionContext context = getInitialContext();
        return context.getTempName();
    }

    private void tryDownloadAttachment(final String url) {
        Command command = new Command("checkAvailability", getName(), getInitialContext());
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                AttachmentAvailabilityActionContext response = (AttachmentAvailabilityActionContext) result;
                if (response.isAvailable()) {
                    Window.Location.replace(url.toString());
                } else {
                    alertError();
                }

            }

            @Override
            public void onFailure(Throwable caught) {
                alertError();
            }
        });
    }

    private void alertError() {
        ApplicationWindow.errorAlert(LocalizeUtil.get(LocalizationKeys.ATTACHMENT_UNAVAILABLE_KEY));
    }
}
