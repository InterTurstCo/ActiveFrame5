package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 22.10.13
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentBoxState extends WidgetState {

    private List<AttachmentModel> newAttachments;
    private LinkedHashMap<Id, AttachmentModel> savedAttachments;

    @Override
    public Value toValue() {
        return null;
    }

    public List<AttachmentModel> getNewAttachments() {
        return newAttachments;
    }

    public void setNewAttachments(List<AttachmentModel> newAttachments) {
        this.newAttachments = newAttachments;
    }

    public LinkedHashMap<Id, AttachmentModel> getSavedAttachments() {
        return savedAttachments;
    }

    public void setSavedAttachments(LinkedHashMap<Id, AttachmentModel> savedAttachments) {
        this.savedAttachments = savedAttachments;
    }

    @Override
    public ArrayList<Value> toValues() {
        if (savedAttachments == null) {
            return null;
        }
        ArrayList<Value> result = new ArrayList<Value>();
        for (Id id : savedAttachments.keySet()) {
            result.add(new ReferenceValue(id));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
