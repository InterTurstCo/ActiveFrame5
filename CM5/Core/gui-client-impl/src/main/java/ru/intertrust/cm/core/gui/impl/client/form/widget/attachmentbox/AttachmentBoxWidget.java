package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenterFactory;
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
            setCurrentStateForNoneEditableWidget(state);
        }
    }

    private void setCurrentStateForEditableWidget(AttachmentBoxState state) {
        AttachmentUploaderView view = (AttachmentUploaderView) impl;
        List<AttachmentItem> attachments = state.getAttachments();
        view.setAllAttachments(state.getAllAttachments());
        view.displayAttachmentItems(attachments);
    }

    private void setCurrentStateForNoneEditableWidget(AttachmentBoxState state) {
        List<AttachmentItem> attachments = state.getAttachments();
        AttachmentNonEditablePanel noneEditablePanel = (AttachmentNonEditablePanel) impl;
        noneEditablePanel.displayAttachmentItems(attachments);
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
            currentState.setNewlyAddedAttachments(attachmentUploaderView.getNewlyAddedAttachments());
            currentState.setAllAttachments(attachmentUploaderView.getAllAttachments());
            return currentState;
        } else {
            return getInitialData();
        }
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
        AttachmentElementPresenterFactory presenterFactory = new AttachmentElementPresenterFactory(attachmentBoxState.getActionLinkConfig(),
                attachmentBoxState.getImagesConfig(), attachmentBoxState.getDeleteButtonConfig(), eventBus);
        if (attachmentBoxState.isInSelectionMode()) {
            return new SelectAttachmentUploaderView(attachmentBoxState, presenterFactory, eventBus);
        } else {
            return new AttachmentUploaderView(attachmentBoxState, presenterFactory, eventBus);
        }
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
        SelectionStyleConfig selectionStyleConfig = attachmentBoxState.getSelectionStyleConfig();
        AttachmentElementPresenterFactory presenterFactory = new AttachmentElementPresenterFactory(attachmentBoxState.getActionLinkConfig(),
                attachmentBoxState.getImagesConfig(), attachmentBoxState.getDeleteButtonConfig(), eventBus);
        return new AttachmentNonEditablePanel(selectionStyleConfig, presenterFactory);
    }

}