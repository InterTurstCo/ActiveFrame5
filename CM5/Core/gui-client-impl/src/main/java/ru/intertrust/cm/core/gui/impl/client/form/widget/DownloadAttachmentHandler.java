package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 09.12.13
 *         Time: 13:15
 */
public class DownloadAttachmentHandler implements ClickHandler {

    private AttachmentItem item;

    public DownloadAttachmentHandler(AttachmentItem item) {
        this.item = item;
    }

    public void setItem(AttachmentItem item) {
        this.item = item;
    }

    @Override
    public void onClick(ClickEvent event) {
        StringBuilder url = new StringBuilder(com.google.gwt.core.client.GWT.getHostPageBaseURL())
                .append("attachment-download?");
        if (item.getId() != null) {
            url.append("id=").append(item.getId().toStringRepresentation());
        }
        if (item.getTemporaryName() != null) {
            url.append("tempName=").append(item.getTemporaryName());
        }

        Window.open(url.toString(), "download File", "");
    }

}
