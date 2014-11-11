package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.AttachmentElementPresenterFactory;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.EditableNonDeletablePresenterFactory;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 30.10.14
 *         Time: 13:12
 */
public class PopupSelectAttachmentUploaderView extends AttachmentUploaderView {

    private Panel selectedItemsPanel = new AbsolutePanel();
    private Panel allItemsPanel;
    private DialogBox selectionDialog = new DialogBox(false, true);
    private List<CheckBox> checkboxes = new ArrayList<>();
    private List<AttachmentItem> tmpSelectedAttachments = new ArrayList<>();
    private AttachmentElementPresenterFactory presenterFactory;
    private Panel mainBoxPanel;

    public PopupSelectAttachmentUploaderView(AttachmentBoxState state, EventBus eventBus) {
        super(state, eventBus);
        presenterFactory = new EditableNonDeletablePresenterFactory(state.getActionLinkConfig(),
                state.getImagesConfig());
        init();
    }

    private void init() {
        mainBoxPanel = super.getAttachmentsPanel();
        Button showPopupButton = new Button("...");
        showPopupButton.getElement().setClassName("lightButton");
        showPopupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                tmpSelectedAttachments.clear();
                tmpSelectedAttachments.addAll(getAttachments());
                showItemsInPopup();
                selectionDialog.center();
            }
        });
        mainBoxPanel.add(showPopupButton);
        mainBoxPanel.add(selectedItemsPanel);
        initSelectionDialog();
    }

    @Override
    protected Panel getAttachmentsPanel() {
        return selectedItemsPanel;
    }

    @Override
    public void displayAttachmentItems() {
        selectedItemsPanel.clear();
        for (Widget element : createSelectedElements()) {
            selectedItemsPanel.add(element);
        }
    }

    protected Panel createNonSelectedElement(AttachmentItem item) {
        Panel element = presenterFactory.createPresenter(item).presentElement();
        element.add(createCheckbox(item, isItemSelected(item)));
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
                getAttachments().clear();
                getAttachments().addAll(tmpSelectedAttachments);
                selectedItemsPanel.clear();
                for (Widget element : createSelectedElements()) {
                    selectedItemsPanel.add(element);
                }
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
        for (AttachmentItem item : getAllAttachments()) {
            allItemsPanel.add(createNonSelectedElement(item));
        }
    }

    private CheckBox createCheckbox(final AttachmentItem item, boolean checked) {
        final CheckBox checkbox = new CheckBox();
        checkbox.setValue(checked);
        checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    if (isSingleChoice()) {
                        uncheckOthers(checkbox);
                        deselectAllTmpAttachments();
                    }
                    selectTmpAttachment(item);
                } else {
                    deselectTmpAttachment(item);
                }
            }
        });

        checkboxes.add(checkbox);
        if (checked && isSingleChoice()) {
            uncheckOthers(checkbox);
        }
        return checkbox;
    }

    private void uncheckOthers(CheckBox checkbox) {
        for (CheckBox chb : checkboxes) {
            if (checkbox != chb) {
                chb.setValue(false);
            }
        }
    }

    private void deselectAllTmpAttachments() {
        tmpSelectedAttachments.clear();
    }

    private void selectTmpAttachment(AttachmentItem attachment) {
        if (isSingleChoice()) {
            tmpSelectedAttachments.clear();
        }
        if (!tmpSelectedAttachments.contains((tmpSelectedAttachments))) {
            tmpSelectedAttachments.add(attachment);
        }
    }

    private void deselectTmpAttachment(AttachmentItem attachment) {
        tmpSelectedAttachments.remove(attachment);
    }

}
