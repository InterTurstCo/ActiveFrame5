package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.13
 *         Time: 13:15
 */
public class AttachmentBoxState extends LinkEditingWidgetState {

    private List<AttachmentModel> attachments;

    public List<AttachmentModel> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentModel> attachments) {
        this.attachments = attachments;
    }

    @Override
    public ArrayList<Id> getIds() {
        if (attachments == null) {
            return null;
        }
        ArrayList<Id> result = new ArrayList<Id>();
        for (AttachmentModel attachmentModel: attachments) {
            Id id = attachmentModel.getId();
            if (id != null)  {
                result.add(id);
            }

        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
