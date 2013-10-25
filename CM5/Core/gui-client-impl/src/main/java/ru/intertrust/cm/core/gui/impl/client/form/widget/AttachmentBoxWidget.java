package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.attachment.AttachmentUploaderView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentModel;
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

        List<AttachmentModel> attachments = state.getAttachments();
        String widthString = displayConfig.getWidth();
        int width = Integer.parseInt(widthString.replaceAll("\\D+",""));
        AttachmentUploaderView attachmentUploaderView = (AttachmentUploaderView) impl;
        attachmentUploaderView.setAttachments(attachments);
        attachmentUploaderView.setWidgetWidth(width);
        attachmentUploaderView.showAttachmentNames();

    }

    @Override
    public WidgetState getCurrentState() {
        AttachmentBoxState state = new AttachmentBoxState();
        AttachmentUploaderView attachmentUploaderView = (AttachmentUploaderView) impl;
        List<AttachmentModel> attachments  = attachmentUploaderView.getAttachments();
        state.setAttachments(attachments);

        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        return new AttachmentUploaderView();
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new AttachmentUploaderView();
    }
}