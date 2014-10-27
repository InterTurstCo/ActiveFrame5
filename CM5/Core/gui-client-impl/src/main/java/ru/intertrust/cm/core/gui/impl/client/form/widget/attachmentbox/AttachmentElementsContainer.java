package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenter;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 17.10.14
 *         Time: 12:40
 */
public interface AttachmentElementsContainer {

    public void displayAttachmentItem(AttachmentElementPresenter presenter);

    public void displayAttachmentItems(List<AttachmentElementPresenter> presenters);


}
