package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypesConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.AddButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ChoiceStyleConfig;
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
    private List<AttachmentItem> attachments = new ArrayList<AttachmentItem>(); //selected for 'check-box' mode, all for 1:N
    private List<AttachmentItem> allAttachments = new ArrayList<AttachmentItem>(); // all from attachment table + all uploaded (not saved yet)
    private List<AttachmentItem> newlyAddedAttachments = new ArrayList<AttachmentItem>(); // uploaded (not saved yet)
    private List<AttachmentItem> newlyDeletedAttachments = new ArrayList<AttachmentItem>(); // uploaded (not saved yet)

    private boolean inSelectionMode; // if true, show checkbox

    private SelectionStyleConfig selectionStyleConfig;
    private ActionLinkConfig actionLinkConfig;
    private AcceptedTypesConfig acceptedTypesConfig;
    private ImagesOnlyConfig imagesConfig;
    private DeleteButtonConfig deleteButtonConfig;
    private AddButtonConfig addButtonConfig;
    private ChoiceStyleConfig choiceStyleConfig;

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

    public List<AttachmentItem> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentItem> attachments) {
        this.attachments = attachments;
    }

    public List<AttachmentItem> getAllAttachments() {
        return allAttachments;
    }

    public void setAllAttachments(List<AttachmentItem> allAttachments) {
        this.allAttachments = allAttachments;
    }

    public List<AttachmentItem> getNewlyAddedAttachments() {
        return newlyAddedAttachments;
    }

    public void setNewlyAddedAttachments(List<AttachmentItem> newlyAddedAttachments) {
        this.newlyAddedAttachments = newlyAddedAttachments;
    }

    public List<AttachmentItem> getNewlyDeletedAttachments() {
        return newlyDeletedAttachments;
    }

    public void setNewlyDeletedAttachments(List<AttachmentItem> newlyDeletedAttachments) {
        this.newlyDeletedAttachments = newlyDeletedAttachments;
    }

    public SelectionStyleConfig getSelectionStyleConfig() {
        return selectionStyleConfig;
    }

    public void setSelectionStyleConfig(SelectionStyleConfig selectionStyleConfig) {
        this.selectionStyleConfig = selectionStyleConfig;
    }

    public boolean isInSelectionMode() {
        return inSelectionMode;
    }

    public void setInSelectionMode(boolean inSelectionMode) {
        this.inSelectionMode = inSelectionMode;
    }

    public AcceptedTypesConfig getAcceptedTypesConfig() {
        return acceptedTypesConfig;
    }

    public void setAcceptedTypesConfig(AcceptedTypesConfig acceptedTypesConfig) {
        this.acceptedTypesConfig = acceptedTypesConfig;
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

    public AddButtonConfig getAddButtonConfig() {
        return addButtonConfig;
    }

    public void setAddButtonConfig(AddButtonConfig addButtonConfig) {
        this.addButtonConfig = addButtonConfig;
    }

    public void setChoiceStyleConfig(ChoiceStyleConfig choiceStyleConfig) {
        this.choiceStyleConfig = choiceStyleConfig;
    }

    public boolean isPopupChoiceStyle() {
        if (choiceStyleConfig == null) {
            return false;
        }
        return "popup".equals(choiceStyleConfig.getName());
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
        if (allAttachments != null ? !allAttachments.equals(that.allAttachments) : that.allAttachments != null) return false;
        if (newlyAddedAttachments != null ? !newlyAddedAttachments.equals(that.newlyAddedAttachments) : that.newlyAddedAttachments != null)
            return false;
        if (newlyDeletedAttachments != null ? !newlyDeletedAttachments.equals(that.newlyDeletedAttachments) : that.newlyDeletedAttachments != null)
            return false;
        if (selectionStyleConfig != null ? !selectionStyleConfig.equals(that.selectionStyleConfig) : that.selectionStyleConfig != null)
            return false;
        if (imagesConfig != null ? !imagesConfig.equals(that.imagesConfig) : that.imagesConfig != null)
            return false;
        if (deleteButtonConfig != null ? !deleteButtonConfig.equals(that.deleteButtonConfig) : that.deleteButtonConfig != null)
            return false;
        if (addButtonConfig != null ? !addButtonConfig.equals(that.addButtonConfig) : that.addButtonConfig != null)
            return false;
        if (choiceStyleConfig != null ? !choiceStyleConfig.equals(that.choiceStyleConfig) : that.choiceStyleConfig != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = selectionStyleConfig != null ? selectionStyleConfig.hashCode() : 0;
        result = 31 * result + (attachments != null ? attachments.hashCode() : 0);
        result = 31 * result + (allAttachments != null ? allAttachments.hashCode() : 0);
        result = 31 * result + (newlyAddedAttachments != null ? newlyAddedAttachments.hashCode() : 0);
        result = 31 * result + (newlyDeletedAttachments != null ? newlyDeletedAttachments.hashCode() : 0);
        result = 31 * result + (actionLinkConfig != null ? actionLinkConfig.hashCode() : 0);
        result = 31 * result + (imagesConfig != null ? imagesConfig.hashCode() : 0);
        result = 31 * result + (deleteButtonConfig != null ? deleteButtonConfig.hashCode() : 0);
        result = 31 * result + (addButtonConfig != null ? addButtonConfig.hashCode() : 0);
        result = 31 * result + (choiceStyleConfig != null ? choiceStyleConfig.hashCode() : 0);
        return result;
    }
}