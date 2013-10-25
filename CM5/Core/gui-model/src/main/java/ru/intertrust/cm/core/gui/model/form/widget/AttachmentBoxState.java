package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.13
 *         Time: 13:15
 */
public class AttachmentBoxState extends WidgetState {

    private List<AttachmentModel> attachments;

    @Override
    public Value toValue() {
        return null;
    }

    public List<AttachmentModel> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentModel> attachments) {
        this.attachments = attachments;
    }

    @Override
    public ArrayList<Value> toValues() {
        if (attachments == null) {
            return null;
        }
        ArrayList<Value> result = new ArrayList<Value>();
        for (AttachmentModel attachmentModel: attachments) {
            Id id = attachmentModel.getId();
            if (id != null)  {
                result.add(new ReferenceValue(id));
            }

        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
