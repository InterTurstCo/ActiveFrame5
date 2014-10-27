package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypesConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.DeleteButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ImagesOnlyConfig;
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
    private List<AttachmentItem> attachments = new ArrayList<AttachmentItem>();
    private ActionLinkConfig actionLinkConfig;
    private AcceptedTypesConfig acceptedTypesConfig;
    private ImagesOnlyConfig imagesConfig;
    private DeleteButtonConfig deleteButtonConfig;
    private boolean displayAddButton;

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

    public AcceptedTypesConfig getAcceptedTypesConfig() {
        return acceptedTypesConfig;
    }

    public void setAcceptedTypesConfig(AcceptedTypesConfig acceptedTypesConfig) {
        this.acceptedTypesConfig = acceptedTypesConfig;
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

    public ImagesOnlyConfig getImagesConfig() {
        return imagesConfig;
    }

    public void setImagesConfig(ImagesOnlyConfig imagesConfig) {
        this.imagesConfig = imagesConfig;
    }

    public DeleteButtonConfig getDeleteButtonConfig() {
        return deleteButtonConfig;
    }

    public void setDeleteButtonConfig(DeleteButtonConfig deleteButtonConfig) {
        this.deleteButtonConfig = deleteButtonConfig;
    }

    public boolean isDisplayAddButton() {
        return displayAddButton;
    }

    public void setDisplayAddButton(boolean displayAddButton) {
        this.displayAddButton = displayAddButton;
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
        if (imagesConfig != null ? !imagesConfig.equals(that.imagesConfig) : that.imagesConfig != null)
            return false;
        if (deleteButtonConfig != null ? !deleteButtonConfig.equals(that.deleteButtonConfig) : that.deleteButtonConfig != null)
            return false;
        if (displayAddButton != that.displayAddButton)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = selectionStyleConfig != null ? selectionStyleConfig.hashCode() : 0;
        result = 31 * result + (attachments != null ? attachments.hashCode() : 0);
        result = 31 * result + (actionLinkConfig != null ? actionLinkConfig.hashCode() : 0);
        result = 31 * result + (imagesConfig != null ? imagesConfig.hashCode() : 0);
        result = 31 * result + (deleteButtonConfig != null ? deleteButtonConfig.hashCode() : 0);
        result = 31 * result + (displayAddButton ? 1 : 0);
        return result;
    }
}

