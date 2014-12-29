package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.model.action.DownloadAttachmentActionContext;
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
        DownloadAttachmentActionContext context = new DownloadAttachmentActionContext();
        context.setId(item.getId());
        context.setTempName(item.getTemporaryName());
        final Action action = ComponentRegistry.instance.get("download.attachment.action");
        action.setInitialContext(context);
        action.perform();
    }

}
