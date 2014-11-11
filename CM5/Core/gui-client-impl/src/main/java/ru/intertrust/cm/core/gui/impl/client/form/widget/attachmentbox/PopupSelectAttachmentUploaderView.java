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
public class PopupSelectAttachmentUploaderView extends AttachmentUploaderView {

    private Panel selectedItemsPanel = new AbsolutePanel();
    private Panel allItemsPanel;
    private DialogBox selectionDialog = new DialogBox(false, true);
    private List<CheckBox> checkboxes = new ArrayList<>();
    private boolean initialized;
    private List<AttachmentItem> tmpSelectedAttachments = new ArrayList<>();

    public PopupSelectAttachmentUploaderView(AttachmentBoxState state, AttachmentElementPresenterFactory
            presenterFactory,
                                             EventBus eventBus) {
        super(state, presenterFactory, eventBus);
    }

    @Override
    protected void displayAttachmentItem(AttachmentItem item){
        selectedItemsPanel.add(createSelectedElement(item));
    }

    @Override
    protected void displaySelectedElements(Panel parentPanel) {
        if (!initialized) {
            parentPanel.add(selectedItemsPanel);

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
            parentPanel.add(showPopupButton);

            initialized = true;
        }
        selectedItemsPanel.clear();
        for (Widget element : createSelectedElements()) {
            selectedItemsPanel.add(element);
        }

    }

    @Override
    protected void displayNonSelectedElements(Panel parentPanel) {
        if (allItemsPanel == null) {
            initSelectionDialog();
        }
    }

    protected Widget createNonSelectedElement(AttachmentItem item) {
        Panel element = presenterFactory.createEditablePresenter(item, new DeleteAttachmentClickHandler(item), false).presentElement();
        element.add(createCheckbox(item, isItemSelected(item)));
        return element;
    }

    @Override
    protected void removeAttachment(AttachmentItem attachment) {
        deselectAttachment(attachment);
    }

    protected void cleanUp() {
        super.cleanUp();
        initialized = false;
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
