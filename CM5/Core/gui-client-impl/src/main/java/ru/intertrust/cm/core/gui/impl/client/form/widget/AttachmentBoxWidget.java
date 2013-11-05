package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.attachment.AttachmentUploaderView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentModel;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.10.13
 *         Time: 13:15
 */
@ComponentName("attachment-box")
public class AttachmentBoxWidget extends BaseWidget {
    protected static final BusinessUniverseServiceAsync SERVICE =
            BusinessUniverseServiceAsync.Impl.getInstance();
    private Timer elapsedTimer;
    private boolean dontShowNewRow;

    @Override
    public Component createNew() {
        return new AttachmentBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        AttachmentBoxState state = (AttachmentBoxState) currentState;

        List<AttachmentModel> attachments = state.getAttachments();
        String widthString = displayConfig.getWidth();
        int width = Integer.parseInt(widthString.replaceAll("\\D+", ""));
        AttachmentUploaderView view = (AttachmentUploaderView) impl;
        view.setAttachments(attachments);
        view.adjustWidgetSizes(width);

        view.cleanUp();
        for (AttachmentModel attachmentModel : attachments) {
            Image deleteRow = createDeleteAttachmentButton(attachmentModel);
            Anchor anchor = createAnchor(attachmentModel);
            view.addRowWithAttachment(attachmentModel, deleteRow, anchor);
        }

    }

    @Override
    public WidgetState getCurrentState() {
        AttachmentBoxState state = new AttachmentBoxState();
        AttachmentUploaderView attachmentUploaderView = (AttachmentUploaderView) impl;
        List<AttachmentModel> attachments = attachmentUploaderView.getAttachments();
        state.setAttachments(attachments);

        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        AttachmentUploaderView attachmentUploaderView = new AttachmentUploaderView();
        attachmentUploaderView.addFormSubmitCompleteHandler(new FormSubmitCompleteHandler());
        attachmentUploaderView.addFormSubmitHandler(new FormSubmitHandler());
        return attachmentUploaderView;
    }

    @Override
    protected Widget asNonEditableWidget() {
        AttachmentUploaderView attachmentUploaderView = new AttachmentUploaderView();
        attachmentUploaderView.addFormSubmitCompleteHandler(new FormSubmitCompleteHandler());
        attachmentUploaderView.addFormSubmitHandler(new FormSubmitHandler());
        return attachmentUploaderView;
    }

    private AttachmentModel handleFileNameFromServer(String filePath) {
        AttachmentModel attachmentModel = new AttachmentModel();
        String[] splitClearName = filePath.split("-_-");
        String clearName = splitClearName[1];
        attachmentModel.setTemporaryName(filePath);
        attachmentModel.setName(clearName);
        ((AttachmentUploaderView) impl).getAttachments().add(attachmentModel);

        return attachmentModel;
    }

    private Image createDeleteAttachmentButton(AttachmentModel model) {

        Image delete = new Image();


        DeleteAttachmentHandler deleteHandler = new DeleteAttachmentHandler(model);
        delete.addClickHandler(deleteHandler);
        return delete;
    }

    private Anchor createAnchor(AttachmentModel model) {
        String anchorTitle = model.getName() + " (" + model.getContentLength() + ")";
        Anchor fileNameAnchor = new Anchor(anchorTitle);
        fileNameAnchor.addClickHandler(new DownloadAttachmentHandler(model));
        return fileNameAnchor;
    }

    private void cancelTimer() {
        if (elapsedTimer != null) {
            elapsedTimer.cancel();
            elapsedTimer = null;
        }
    }

    private void setUpProgressOfUpload(final boolean isUploadCanceled) {

        SERVICE.getAttachmentUploadPercentage(isUploadCanceled, new AsyncCallback<AttachmentUploadPercentage>() {

            @Override
            public void onFailure(final Throwable t) {
                cancelTimer();
            }

            @Override
            public void onSuccess(AttachmentUploadPercentage percentage) {
                 if (isUploadCanceled) {
                     return;
                 }
                Integer percentageValue = percentage.getPercentage();
                System.out.println("percentage " + percentageValue);
                ((AttachmentUploaderView) impl).getUploaderProgressBar().update(percentageValue);
            }
        });
    }

    private class DownloadAttachmentHandler implements ClickHandler {

        AttachmentModel model;

        public DownloadAttachmentHandler(AttachmentModel model) {
            this.model = model;
        }

        @Override
        public void onClick(ClickEvent event) {
            Id id = model.getId();
            Window.open(GWT.getHostPageBaseURL() + "attachment-download/" + id.toStringRepresentation(),
                    "download File", "");
        }

    }

    private class FormSubmitHandler implements FormPanel.SubmitHandler {

        @Override
        public void onSubmit(FormPanel.SubmitEvent event) {

            AttachmentUploaderView view = (AttachmentUploaderView) impl;
            String filePath = view.getFileUpload().getFilename();
            AttachmentModel model = new AttachmentModel();
            model.setName(filePath);
            Image deleteAttachment = createDeleteAttachmentButton(model);

            view.addRowWithAttachment(model, deleteAttachment, null);
            elapsedTimer = new Timer() {
                public void run() {
                    setUpProgressOfUpload(false);
                }
            };
            elapsedTimer.scheduleRepeating(1000);

        }
    }

    private class FormSubmitCompleteHandler implements FormPanel.SubmitCompleteHandler {

        public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {

            if (dontShowNewRow) {
                dontShowNewRow = false;
                return;
            }
            String filePath = event.getResults();
            AttachmentModel model = handleFileNameFromServer(filePath);
            Image deleteAttachment = createDeleteAttachmentButton(model);
            AttachmentUploaderView view = (AttachmentUploaderView) impl;
            view.getUploaderProgressBar().getParent().getParent().removeFromParent();
            view.addRowWithAttachment(model, deleteAttachment, null);
            elapsedTimer.cancel();
            view.getUploaderProgressBar().update(100);

        }
    }

    private class DeleteAttachmentHandler implements ClickHandler {

        AttachmentModel model;

        public DeleteAttachmentHandler(AttachmentModel model) {
            this.model = model;

        }

        @Override
        public void onClick(ClickEvent event) {
            Image delete = (Image) event.getSource();
            delete.getParent().getParent().removeFromParent();
            AttachmentUploaderView view = ((AttachmentUploaderView) impl);
            view.getAttachments().remove(model);

            if (model.getTemporaryName() == null && model.getId() == null) {
                dontShowNewRow = true;
                setUpProgressOfUpload(true);
                cancelTimer();

            }

        }
    }
}