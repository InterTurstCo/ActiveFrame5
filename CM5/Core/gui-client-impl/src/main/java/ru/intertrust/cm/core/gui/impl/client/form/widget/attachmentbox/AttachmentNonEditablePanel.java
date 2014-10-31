package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.AbstractNoneEditablePanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenterFactory;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
public class AttachmentNonEditablePanel extends AbstractNoneEditablePanel implements AttachmentElementsContainer {
    private AttachmentElementPresenterFactory factory;

    public AttachmentNonEditablePanel(SelectionStyleConfig selectionStyleConfig) {
        super(selectionStyleConfig);
    }

    @Override
    public void displayAttachmentItem(AttachmentItem item){
        mainBoxPanel.add(factory.createNonEditablePresenter(item).presentElement());
    }

    @Override
    public void displayAttachmentItemInProgress(AttachmentItem item) {
        // do nothing - N/A
    }

    @Override
    public void displayAttachmentItems(List<AttachmentItem> items) {
        for (AttachmentItem item : items) {
            displayAttachmentItem(item);
        }
    }

    @Override
    public void setPresenterFactory(AttachmentElementPresenterFactory factory) {
        this.factory = factory;
    }
}
