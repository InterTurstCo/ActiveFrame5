package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypesConfig;
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

    private AttachmentElementPresenterFactory presenterFactory;

    @Override
    public Component createNew() {
        return new AttachmentBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        AttachmentBoxState state = (AttachmentBoxState) currentState;
        presenterFactory = new AttachmentElementPresenterFactory(state.getAttachments(), state.getActionLinkConfig(),
                state.getImagesConfig(), state.getDeleteButtonConfig(), eventBus);
        if (isEditable()) {
            setCurrentStateForEditableWidget(state);
        } else {
            setCurrentStateForNoneEditableWidget(state);
        }
    }

    private void setCurrentStateForEditableWidget(AttachmentBoxState state) {
        AttachmentUploaderView view = (AttachmentUploaderView) impl;
        List<AttachmentItem> attachments = state.getAttachments();
        view.setPresenterFactory(presenterFactory);
        view.setSingleChoice(state.isSingleChoice());
        view.displayAttachmentItems(attachments);
    }

    private void setCurrentStateForNoneEditableWidget(AttachmentBoxState state) {
        List<AttachmentItem> attachments = state.getAttachments();
        AttachmentNonEditablePanel noneEditablePanel = (AttachmentNonEditablePanel) impl;
        noneEditablePanel.setPresenterFactory(presenterFactory);
        noneEditablePanel.displayAttachmentItems(attachments);
    }

    @Override
    protected boolean isChanged() { //TODO: unneeded confirmation sometimes appears
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
        SelectionStyleConfig selectionStyleConfig = attachmentBoxState.getSelectionStyleConfig();
        AcceptedTypesConfig acceptedTypesConfig = attachmentBoxState.getAcceptedTypesConfig();
        if (attachmentBoxState.isInSelectionMode()) {
            return new SelectAttachmentUploaderView(attachmentBoxState.getAttachments(),
                attachmentBoxState.getAllAttachments(), selectionStyleConfig,
                acceptedTypesConfig, attachmentBoxState.isDisplayAddButton(), eventBus);
        } else {
            return new AttachmentUploaderView(attachmentBoxState.getAttachments(), attachmentBoxState.getAllAttachments(),
                    selectionStyleConfig, acceptedTypesConfig, attachmentBoxState.isDisplayAddButton(), eventBus);
        }
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
        SelectionStyleConfig selectionStyleConfig = attachmentBoxState.getSelectionStyleConfig();
        return new AttachmentNonEditablePanel(selectionStyleConfig);
    }

}