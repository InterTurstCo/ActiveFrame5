package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.AttachmentElementPresenterFactory;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.EditableNonDeletablePresenterFactory;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.CaptionCloseButton;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.ArrayList;
import java.util.List;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CANCEL_BUTTON_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.OK_BUTTON_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CANCEL_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.OK_BUTTON;

/**
 * @author Lesia Puhova
 *         Date: 30.10.14
 *         Time: 13:12
 */
public class PopupSelectAttachmentUploaderView extends AttachmentUploaderView {

    private Panel allItemsPanel;
    private DialogBox selectionDialog = new DialogBox(false, true);
    private List<CheckBox> checkboxes = new ArrayList<>();
    private List<AttachmentItem> tmpSelectedAttachments = new ArrayList<>();
    private AttachmentElementPresenterFactory presenterFactory;
    private Button cancelButton;

    public PopupSelectAttachmentUploaderView(AttachmentBoxState state, EventBus eventBus, BaseWidget parent) {
        super(state, eventBus, parent);
        presenterFactory = new EditableNonDeletablePresenterFactory(state.getActionLinkConfig(),
                state.getImagesConfig(), state.getDigitalSignaturesConfig());
        initPopupSelectAttachmentUploaderView();
    }

    private void initPopupSelectAttachmentUploaderView() {
        Button showPopupButton = new Button("...");
        showPopupButton.getElement().setClassName("lightButton selectionButton");
        showPopupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                tmpSelectedAttachments.clear();
                tmpSelectedAttachments.addAll(getAttachments());
                showItemsInPopup();
                selectionDialog.center();
            }
        });
        super.getControlPanel().add(showPopupButton);
        initSelectionDialog();
    }

    protected Panel createNonSelectedElement(AttachmentItem item) {
        Panel element = presenterFactory.createPresenter(item, null, getAllAttachments()).presentElement();
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

        Button okButton = new Button(LocalizeUtil.get(OK_BUTTON_KEY, OK_BUTTON));
        okButton.getElement().setClassName("lightButton");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getAttachments().clear();
                getAttachments().addAll(tmpSelectedAttachments);
                displayAttachmentItems();
                selectionDialog.hide();
            }
        });
        cancelButton = new Button(LocalizeUtil.get(CANCEL_BUTTON_KEY, CANCEL_BUTTON));
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
        initCaption();
    }


    private void initCaption(){
        HTML caption = (HTML) selectionDialog.getCaption();
        CaptionCloseButton captionCloseButton = new CaptionCloseButton();
        captionCloseButton.addClickListener(new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                cancelButton.click();
            }
        });

        caption.getElement().appendChild(captionCloseButton.getElement());

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
        if (!tmpSelectedAttachments.contains((attachment))) {
            tmpSelectedAttachments.add(attachment);
        }
    }

    private void deselectTmpAttachment(AttachmentItem attachment) {
        tmpSelectedAttachments.remove(attachment);
    }

}
