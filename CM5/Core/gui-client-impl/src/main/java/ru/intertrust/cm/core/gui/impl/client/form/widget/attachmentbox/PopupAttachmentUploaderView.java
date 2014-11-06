package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenterFactory;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 30.10.14
 *         Time: 13:12
 */
public class PopupAttachmentUploaderView extends AttachmentUploaderView {

    private Panel selectedItemsPanel;
    private Panel unselectedItemsPanel;

    public PopupAttachmentUploaderView(AttachmentBoxState state, AttachmentElementPresenterFactory presenterFactory,
                                       EventBus eventBus) {
        super(state, presenterFactory, eventBus);
    }

    @Override
    protected void displaySelectedElements(Panel parentPanel) {
        if (selectedItemsPanel == null) {
            selectedItemsPanel = new AbsolutePanel();
            parentPanel.add(selectedItemsPanel);
        }
        selectedItemsPanel. clear();
        for (Widget element : createSelectedElements()) {
            selectedItemsPanel.add(element);
        }
    }

    @Override
    protected void displayNonSelectedElements(Panel parentPanel) {
        if (unselectedItemsPanel == null) {
            unselectedItemsPanel = new AbsolutePanel(); //TODO: should be popup panel with enclosed absolute panel
            parentPanel.add(unselectedItemsPanel);
        }
        unselectedItemsPanel. clear();
        for (Widget element : createNonSelectedElements()) {
            unselectedItemsPanel.add(element);
        }
    }

    @Override
    protected List<Widget> createNonSelectedElements() {
        List<Widget> elements = new ArrayList<>(getAllAttachments().size());
        for (AttachmentItem item : getAllAttachments()) {
            if (!getAttachments().contains(item)) {
                elements.add(createNonSelectedElement(item));
            }
        }
        return elements;
    }
}
