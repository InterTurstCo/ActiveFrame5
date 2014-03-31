package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.13
 *         Time: 13:15
 */
public class AttachmentBoxState extends LinkEditingWidgetState {
    private SelectionStyleConfig selectionStyleConfig;
    private List<AttachmentItem> attachments;
    private ActionLinkConfig actionLinkConfig;

    public SelectionStyleConfig getSelectionStyleConfig() {
        return selectionStyleConfig;
    }

    public void setSelectionStyleConfig(SelectionStyleConfig selectionStyleConfig) {
        this.selectionStyleConfig = selectionStyleConfig;
    }

    public List<AttachmentItem> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentItem> attachments) {
        this.attachments = attachments;
    }

    @Override
    public ArrayList<Id> getIds() {
        if (attachments == null) {
            return null;
        }
        ArrayList<Id> result = new ArrayList<Id>();
        for (AttachmentItem attachmentItem : attachments) {
            Id id = attachmentItem.getId();
            if (id != null) {
                result.add(id);
            }

        }
        return result;
    }

    public ActionLinkConfig getActionLinkConfig() {
        return actionLinkConfig;
    }

    public void setActionLinkConfig(ActionLinkConfig actionLinkConfig) {
        this.actionLinkConfig = actionLinkConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AttachmentBoxState that = (AttachmentBoxState) o;

        if (actionLinkConfig != null ? !actionLinkConfig.equals(that.actionLinkConfig) : that.actionLinkConfig != null)
            return false;
        if (attachments != null ? !attachments.equals(that.attachments) : that.attachments != null) return false;
        if (selectionStyleConfig != null ? !selectionStyleConfig.equals(that.selectionStyleConfig) : that.selectionStyleConfig != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = selectionStyleConfig != null ? selectionStyleConfig.hashCode() : 0;
        result = 31 * result + (attachments != null ? attachments.hashCode() : 0);
        result = 31 * result + (actionLinkConfig != null ? actionLinkConfig.hashCode() : 0);
        return result;
    }
}

