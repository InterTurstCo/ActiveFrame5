package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.AttachmentElementPresenterFactory;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.EditableNonDeletablePresenterFactory;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

/**
 * @author Lesia Puhova
 *         Date: 11.11.14
 *         Time: 12:31
 */
public class PopupAttachmentUploaderView extends AttachmentUploaderView {

    private AttachmentElementPresenterFactory presenterFactory;
    private Panel allItemsPanel;
    private DialogBox selectionDialog = new DialogBox(false, true);

    public PopupAttachmentUploaderView(AttachmentBoxState state, EventBus eventBus) {
        super(state, eventBus);
        presenterFactory = new EditableNonDeletablePresenterFactory(state.getActionLinkConfig(),
                state.getImagesConfig());
        init();
    }

    private void init() {
        Button showPopupButton = new Button("...");
        showPopupButton.getElement().setClassName("lightButton selectionButton");
        showPopupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showItemsInPopup();
                selectionDialog.center();
            }
        });
        super.getControlPanel().add(showPopupButton);
        initSelectionDialog();
    }

    protected Panel createNonSelectedElement(AttachmentItem item) {
        Panel element = presenterFactory.createPresenter(item, null, getAllAttachments()).presentElement();
        return element;
    }

    @Override
    protected void removeAttachment(AttachmentItem attachment) {
        deselectAttachment(attachment);
    }

    private void initSelectionDialog() {
        Panel panel = new AbsolutePanel();
        allItemsPanel = new AbsolutePanel();
        allItemsPanel.getElement().setClassName("imageSelectionContentPanel");
        allItemsPanel.getElement().getStyle().clearOverflow();
        panel.add(allItemsPanel);

        Button okButton = new Button("OK");
        okButton.getElement().setClassName("lightButton");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionDialog.hide();
            }
        });
        Button cancelButton = new Button("Отменить");
        cancelButton.getElement().setClassName("darkButton");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionDialog.hide();
            }
        });
        Panel buttonPanel = new AbsolutePanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        selectionDialog.setWidget(panel);
        selectionDialog.setStyleName("popupWindow");
        selectionDialog.setStyleName("popupWindow imageSelection");
    }

    private void showItemsInPopup() {
        allItemsPanel.clear();
        for (AttachmentItem item : getAttachments()) {
            allItemsPanel.add(createNonSelectedElement(item));
        }
    }

}
