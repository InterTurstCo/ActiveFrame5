package ru.intertrust.cm.core.gui.model.plugin;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

public class UploadData implements Dto {
    private static final long serialVersionUID = -770687312276829421L;
    private List<AttachmentItem> attachmentItems;

    public List<AttachmentItem> getAttachmentItems() {
        return attachmentItems;
    }

    public void setAttachmentItems(List<AttachmentItem> attachmentItems) {
        this.attachmentItems = attachmentItems;
    }
}
