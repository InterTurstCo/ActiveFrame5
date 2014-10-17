package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.AbstractNoneEditablePanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenter;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
public class AttachmentNonEditablePanel extends AbstractNoneEditablePanel implements AttachmentElementsContainer {
    public AttachmentNonEditablePanel(SelectionStyleConfig selectionStyleConfig) {
        super(selectionStyleConfig);
    }

    @Override
    public void displayAttachmentItem(AttachmentElementPresenter presenter){
        mainBoxPanel.add(presenter.presentElement());
    }

    @Override
    public void displayAttachmentItems(List<AttachmentElementPresenter> presenters) {
        for (AttachmentElementPresenter presenter : presenters) {
            displayAttachmentItem(presenter);
        }
    }
}
