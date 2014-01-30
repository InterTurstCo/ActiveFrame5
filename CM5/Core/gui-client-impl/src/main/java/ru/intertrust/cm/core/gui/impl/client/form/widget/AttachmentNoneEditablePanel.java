package ru.intertrust.cm.core.gui.impl.client.form.widget;

import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 29.01.14
 *         Time: 13:15
 */
public class AttachmentNoneEditablePanel extends NoneEditablePanel {
    private List<AttachmentItem> attachmentItems;

    public List<AttachmentItem> getAttachmentItems() {
        return attachmentItems;
    }

    public void setAttachmentItems(List<AttachmentItem> attachmentItems) {
        this.attachmentItems = attachmentItems;
    }

    /*@Override
    public ArrayList<Id> getChosenIds() {
        ArrayList<Id> chosenIds = new ArrayList<Id>();
        for (AttachmentItem attachmentItem : attachmentItems) {
            chosenIds.add(attachmentItem.getId());
        }
        return chosenIds;
    }*/

    @Override
    public void showSelectedItems(String howToDisplay) {

        initDisplayStyle(howToDisplay);
        for (AttachmentItem attachmentItem : attachmentItems) {
            String itemRepresentation = attachmentItem.getName();
            displayItem(itemRepresentation);
        }
    }
}
