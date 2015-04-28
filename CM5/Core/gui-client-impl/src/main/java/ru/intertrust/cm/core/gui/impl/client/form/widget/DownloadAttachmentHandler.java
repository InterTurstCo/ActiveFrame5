package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.event.ShowAttachmentEvent;
import ru.intertrust.cm.core.gui.model.action.DownloadAttachmentActionContext;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentViewerState;

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
        DownloadAttachmentActionContext context = new DownloadAttachmentActionContext();
        if (item.getAttachmentViewerRefConfig() != null && AttachmentViewerState.isTypeEnabled(item.getMimeType())) {
            Application.getInstance().getEventBus().fireEvent(new ShowAttachmentEvent(item.getId(),
                    item.getAttachmentViewerRefConfig().getId()));
        } else {
            context.setId(item.getId());
            context.setTempName(item.getTemporaryName());
            final Action action = ComponentRegistry.instance.get("download.attachment.action");
            action.setInitialContext(context);
            action.perform();
        }
    }

}
