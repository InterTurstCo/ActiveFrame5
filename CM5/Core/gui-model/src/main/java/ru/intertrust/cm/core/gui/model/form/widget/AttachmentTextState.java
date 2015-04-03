package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;

/**
 * @author Denis Mitavskiy
 *         Date: 27.03.2015
 *         Time: 14:10
 */
public class AttachmentTextState extends LinkEditingWidgetState {
    private Id attachmentId;
    private String text;
    private boolean changed;

    public AttachmentTextState() {
    }

    public AttachmentTextState(Id attachmentId, String text, boolean changed) {
        this.attachmentId = attachmentId;
        this.text = text;
        this.changed = changed;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public Id getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Id attachmentId) {
        this.attachmentId = attachmentId;
    }

    @Override
    public ArrayList<Id> getIds() {
        if (attachmentId == null) {
            return null;
        }
        ArrayList<Id> ids = new ArrayList<>(1);
        ids.add(attachmentId);
        return ids;
    }
}
