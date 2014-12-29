package ru.intertrust.cm.core.gui.impl.client.action;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.DownloadAttachmentActionContext;

/**
 * @author Lesia Puhova
 *         Date: 23.12.2014
 *         Time: 16:51
 *
 *  Действие скачивания вложения
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
            url.append("tempName=").append(getTemporaryName()).append("&");
        } else if (getId() != null) {
            url.append("id=").append(getId().toStringRepresentation());
        }

        if (getId() != null || getTemporaryName() != null) {
            Window.open(url.toString(), "download File", "");
        } else {
            Window.alert("Вложений не обнаружено");
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
}
