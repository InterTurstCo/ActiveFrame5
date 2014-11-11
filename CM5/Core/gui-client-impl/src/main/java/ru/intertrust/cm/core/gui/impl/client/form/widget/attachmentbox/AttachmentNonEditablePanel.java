package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.AbstractNoneEditablePanel;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.AttachmentElementPresenterFactory;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.NonEditablePresenterFactory;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.14
 *         Time: 13:15
 */
public class AttachmentNonEditablePanel extends AbstractNoneEditablePanel implements AttachmentElementsContainer {
    private AttachmentElementPresenterFactory factory;
    private List<AttachmentItem> items;

    public AttachmentNonEditablePanel(SelectionStyleConfig selectionStyleConfig, AttachmentBoxState state) {
        super(selectionStyleConfig);
        factory = new NonEditablePresenterFactory(state.getImagesConfig());
        items = state.getAttachments();
    }

    private void displayAttachmentItem(AttachmentItem item){
        mainBoxPanel.add(factory.createPresenter(item).presentElement());
    }

    @Override
    public void displayAttachmentItems() {
        for (AttachmentItem item : items) {
            displayAttachmentItem(item);
        }
    }

}
