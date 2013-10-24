package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.attachment.AttachmentUploaderView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentModel;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 22.10.13
 * Time: 18:53
 * To change this template use File | Settings | File Templates.
 */
@ComponentName("attachment-box")
public class AttachmentBoxWidget extends BaseWidget {
    LinkedHashMap<Id, AttachmentModel> savedAttachments = new  LinkedHashMap<Id, AttachmentModel>();
    List<AttachmentModel> newAttachments = new ArrayList<AttachmentModel>();
    @Override
    public Component createNew() {
        return new AttachmentBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        AttachmentBoxState state = (AttachmentBoxState) currentState;

        savedAttachments = state.getSavedAttachments();

        AttachmentUploaderView attachmentUploaderView = (AttachmentUploaderView) impl;
        attachmentUploaderView.setSavedAttachments(savedAttachments);
        attachmentUploaderView.showAttachmentNames();

    }

    @Override
    public WidgetState getCurrentState() {
        AttachmentBoxState state = new AttachmentBoxState();
        AttachmentUploaderView attachmentUploaderView = (AttachmentUploaderView) impl;
        savedAttachments   = attachmentUploaderView.getSavedAttachments();
        newAttachments = attachmentUploaderView.getNewAttachments();
        state.setSavedAttachments(savedAttachments);
        state.setNewAttachments(newAttachments);

        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        return new AttachmentUploaderView(savedAttachments);
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new AttachmentUploaderView(savedAttachments);
    }
}