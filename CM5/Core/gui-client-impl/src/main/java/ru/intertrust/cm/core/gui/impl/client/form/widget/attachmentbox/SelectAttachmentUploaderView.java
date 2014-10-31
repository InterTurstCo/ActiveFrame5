package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypesConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 30.10.14
 *         Time: 13:12
 */
public class SelectAttachmentUploaderView extends AttachmentUploaderView {

    private List<CheckBox> checkboxes = new ArrayList<>();

    public SelectAttachmentUploaderView(List<AttachmentItem> attachments, List<AttachmentItem> allAttachments,
                                        SelectionStyleConfig selectionStyleConfig, AcceptedTypesConfig acceptedTypesConfig, boolean displayAddButton, EventBus eventBus) {
        super(attachments, allAttachments, selectionStyleConfig, acceptedTypesConfig, displayAddButton, eventBus);
    }

    @Override
    protected Widget createAttachmentElement(AttachmentItem item) {
        Panel element = (Panel)super.createAttachmentElement(item);
        element.add(createCheckbox(item, true));
        return element;
    }

    @Override
    protected List<Widget> createNonSelectedElements() {
        List<Widget> elements = new ArrayList<>(getAllAttachments().size());
        for (AttachmentItem item : getAllAttachments()) {
            if (!getAttachments().contains(item)) {
                Panel element = (Panel)super.createAttachmentElement(item);
                element.add(createCheckbox(item, false));
                elements.add(element);
            }
        }
        return elements;
    }

    private CheckBox createCheckbox(final AttachmentItem item, boolean checked) {
        final CheckBox checkbox = new CheckBox();
        checkbox.setValue(checked);
        checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    getAttachments().add(item);
                    if (isSingleChoice()) {
                        //TODO:  may be it would be better to use radio-button group?
                        uncheckOthers(checkbox);
                    }
                } else {
                    getAttachments().remove(item);
                }
            }
        });

        checkboxes.add(checkbox);
        if (checked && isSingleChoice()) {
            uncheckOthers(checkbox);
        }
        return checkbox;
    }

    @Override
    protected boolean showRewriteConfirmation() {
        return false;
    }

    private void uncheckOthers(CheckBox checkbox) {
        for (CheckBox chb : checkboxes) {
            if (checkbox != chb) {
                chb.setValue(false, true);
            }
        }
    }
}
