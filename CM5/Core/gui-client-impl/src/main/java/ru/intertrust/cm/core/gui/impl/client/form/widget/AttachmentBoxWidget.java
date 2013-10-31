package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Id;
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
        int partWidth = width/2;
        AttachmentUploaderView attachmentUploaderView = (AttachmentUploaderView) impl;
        attachmentUploaderView.setAttachments(attachments);
        attachmentUploaderView.setWidgetWidth(width);

        attachmentUploaderView.getAttachmentsPanel().clear();
        for (AttachmentModel attachmentModel : attachments) {
            Image deleteRow = createDeleteAttachmentButton(attachmentModel);
            Anchor anchor = createAnchor(attachmentModel, partWidth);
            String contentLength = attachmentModel.getContentLength();
            attachmentUploaderView.addRowWithAttachment(attachmentModel, deleteRow, anchor, contentLength);
        }

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
        AttachmentUploaderView attachmentUploaderView = new AttachmentUploaderView();
        attachmentUploaderView.addFormSubmitCompleteHandler(new FormSubmitCompleteHandler());
        return attachmentUploaderView;
    }

    @Override
    protected Widget asNonEditableWidget() {
        AttachmentUploaderView attachmentUploaderView = new AttachmentUploaderView();
        attachmentUploaderView.addFormSubmitCompleteHandler(new FormSubmitCompleteHandler());
        return attachmentUploaderView;
    }

    private AttachmentModel handleFileNameFromServer(String filePath) {
        AttachmentModel attachmentModel = new AttachmentModel();
        String [] splitClearName = filePath.split("-_-");
        String clearName = splitClearName[1];
        attachmentModel.setTemporaryName(filePath);
        attachmentModel.setName(clearName);
        ((AttachmentUploaderView) impl).getAttachments().add(attachmentModel);

        return  attachmentModel;
    }

    private Image createDeleteAttachmentButton(AttachmentModel model) {

        Image delete = new Image();
        delete.setUrl("button_cancel.png");
        delete.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);

        DeleteAttachmentHandler deleteHandler = new DeleteAttachmentHandler(model);
        delete.addClickHandler(deleteHandler);
        return delete;
    }

    private Anchor createAnchor(AttachmentModel model, int partWidth) {

        Anchor fileNameAnchor = new Anchor(model.getName());
        Style style = fileNameAnchor.getElement().getStyle();
        style.setOverflow(Style.Overflow.HIDDEN);

        fileNameAnchor.setWidth(partWidth + "px");
        fileNameAnchor.addClickHandler(new DownloadAttachmentHandler (model));
        return fileNameAnchor;
    }


    private class DownloadAttachmentHandler implements ClickHandler {

        AttachmentModel model;
        public DownloadAttachmentHandler (AttachmentModel model) {
            this.model = model;
        }
        @Override
        public void onClick(ClickEvent event) {
            Id id = model.getId();
            Window.open(GWT.getHostPageBaseURL() + "attachment-download/" + id.toStringRepresentation(),
                    "download File", "");
        }

    }

    private class FormSubmitCompleteHandler implements FormPanel.SubmitCompleteHandler {

        public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
            String filePath = event.getResults();
            AttachmentModel model = handleFileNameFromServer(filePath);

            Image deleteAttachment = createDeleteAttachmentButton(model);

            ((AttachmentUploaderView) impl).addRowWithAttachment(model, deleteAttachment, null, null);
        }
    }

    private class DeleteAttachmentHandler implements ClickHandler {

        AttachmentModel model;

        public DeleteAttachmentHandler(AttachmentModel model) {
            this.model = model;

        }
        @Override
        public void onClick(ClickEvent event) {
            Image delete = (Image)event.getSource();
            delete.getParent().getParent().removeFromParent();
            ((AttachmentUploaderView) impl).getAttachments().remove(model);

        }
    }
}