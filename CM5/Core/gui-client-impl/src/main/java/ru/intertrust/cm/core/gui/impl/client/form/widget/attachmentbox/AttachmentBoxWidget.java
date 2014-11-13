package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.10.13
 *         Time: 13:15
 */
@ComponentName("attachment-box")
public class AttachmentBoxWidget extends BaseWidget {

    @Override
    public Component createNew() {
        return new AttachmentBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        AttachmentBoxState state = (AttachmentBoxState) currentState;
        if (isEditable()) {
            setCurrentStateForEditableWidget(state);
        } else {
            setCurrentStateForNonEditableWidget(state);
        }
    }

    private void setCurrentStateForEditableWidget(AttachmentBoxState state) {
        AttachmentUploaderView view = (AttachmentUploaderView) impl;
        view.setAttachments(state.getAttachments());
        view.setAllAttachments(state.getAllAttachments());
        view.displayAttachmentItems();
    }

    private void setCurrentStateForNonEditableWidget(AttachmentBoxState state) {
        AttachmentNonEditablePanel nonEditablePanel = (AttachmentNonEditablePanel) impl;
        nonEditablePanel.setAttachments(state.getAttachments());
        nonEditablePanel.displayAttachmentItems();
    }

    @Override
    protected boolean isChanged() {
        AttachmentUploaderView attachmentUploaderView = (AttachmentUploaderView) impl;
        List<AttachmentItem> currentValues = attachmentUploaderView.getAttachments();
        List<AttachmentItem> initValues = getInitialData() == null
                ? null : ((AttachmentBoxState) getInitialData()).getAttachments();
        return currentValues == null ? initValues != null : !currentValues.equals(initValues);
    }

    @Override
    protected WidgetState createNewState() {
        if (isEditable()) {
            AttachmentBoxState currentState = new AttachmentBoxState();
            AttachmentUploaderView attachmentUploaderView = (AttachmentUploaderView) impl;
            currentState.setAttachments(attachmentUploaderView.getAttachments());
            currentState.setAllAttachments(attachmentUploaderView.getAllAttachments());
            return currentState;
        } else {
            return getInitialData();
        }
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
        if (attachmentBoxState.isPopupChoiceStyle()) {
            if (attachmentBoxState.isInSelectionMode()) {
                return new PopupSelectAttachmentUploaderView(attachmentBoxState, eventBus);
            } else {
                return new PopupAttachmentUploaderView(attachmentBoxState, eventBus);
            }
        }
        if (attachmentBoxState.isInSelectionMode()){
            return new SelectAttachmentUploaderView(attachmentBoxState, eventBus);
        } else {
            return new AttachmentUploaderView(attachmentBoxState, eventBus);
        }
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
        SelectionStyleConfig selectionStyleConfig = attachmentBoxState.getSelectionStyleConfig();
        return new AttachmentNonEditablePanel(selectionStyleConfig, attachmentBoxState);
    }

}