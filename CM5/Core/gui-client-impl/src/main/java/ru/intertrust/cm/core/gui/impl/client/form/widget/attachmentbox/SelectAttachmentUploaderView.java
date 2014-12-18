package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
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

    public SelectAttachmentUploaderView(AttachmentBoxState state, EventBus eventBus, BaseWidget parent) {
        super(state, eventBus, parent);
    }

    @Override
    protected Panel createSelectedElement(AttachmentItem item) {
        Panel element = super.createSelectedElement(item);
        element.add(createCheckbox(item, true));
        return element;
    }

    @Override
    protected Panel createNonSelectedElement(AttachmentItem item) {
        Panel element = super.createSelectedElement(item);
        element.add(createCheckbox(item, false));
        return element;
    }

    private CheckBox createCheckbox(final AttachmentItem item, boolean checked) {
        final CheckBox checkbox = new CheckBox();
        checkbox.setValue(checked);
        checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    if (isSingleChoice()) {
                        //TODO:  may be it would be better to use radio-button group?
                        uncheckOthers(checkbox);
                        deselectAllAttachments();
                    }
                    selectAttachment(item);
                } else {
                    deselectAttachment(item);
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
                chb.setValue(false);
            }
        }
    }
}
