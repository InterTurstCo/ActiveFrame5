package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

/**
 * Created by andrey on 01.04.14.
 */
public abstract class AttachmentAction extends Action {
    protected AttachmentItem attachmentItem;

    public void setAttachmentItem(AttachmentItem attachmentItem) {
        this.attachmentItem = attachmentItem;
    }
}
