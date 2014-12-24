package ru.intertrust.cm.core.gui.impl.client.form.widget.linkedtable;

import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Lesia Puhova
 *         Date: 23.12.2014
 *         Time: 16:51
 */
@ComponentName("download.attachment.action")
public class DownloadAttachmentAction extends LinkedTableAction {


    @Override
    protected void execute(Id id, int rowIndex) {
        StringBuilder url = new StringBuilder(com.google.gwt.core.client.GWT.getHostPageBaseURL())
                .append("attachment-download?");
        if (id != null) {
            url.append("id=").append(id.toStringRepresentation());
        }
//        if (getTemporaryName() != null) {
//            url.append("tempName=").append(getTemporaryName());
//        }

        Window.open(url.toString(), "download File", "");
    }

    @Override
    protected String getServerComponentName() {
        return "download.attachment.action";
    }


//    private String getTemporaryName() {
//        DownloadAttachmentActionContext context = getInitialContext();
//        return context.getTempName();
//    }

    @Override
    public String getName() {
        return "download.attachment.action";
    }

    @Override
    public Component createNew() {
        return new DownloadAttachmentAction();
    }
}
