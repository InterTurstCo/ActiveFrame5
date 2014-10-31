package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenterFactory;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 17.10.14
 *         Time: 12:40
 */
public interface AttachmentElementsContainer {

    public void displayAttachmentItem(AttachmentItem item);

    public void displayAttachmentItemInProgress(AttachmentItem item);

    public void displayAttachmentItems(List<AttachmentItem> items);

    public void setPresenterFactory(AttachmentElementPresenterFactory factory);

}
