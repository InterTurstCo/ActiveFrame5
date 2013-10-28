package ru.intertrust.cm.core.gui.impl.client.attachment;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentModel;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
public class AttachmentUploaderView extends Composite {

    private VerticalPanel attachmentsPanel;
    private Image addFile;
    private FileUpload fileUpload;
    private FormPanel form;
    private int widgetWidth;
    private int partWidth;

    private List<AttachmentModel> attachments;

    private VerticalPanel root;
     public  AttachmentUploaderView() {
         init();
    }
    public AttachmentUploaderView(List<AttachmentModel> attachments) {
        this.attachments = attachments;

        init();
    }

    public int getWidgetWidth() {
        return widgetWidth;
    }

    public void setWidgetWidth(int widgetWidth) {
        this.widgetWidth = widgetWidth;
    }

    public List<AttachmentModel> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentModel> attachments) {
        this.attachments = attachments;
    }

    public void init() {
        root = new VerticalPanel();

        this.initWidget(root);

        attachmentsPanel = new VerticalPanel();
        initSubmitForm();
        initFileUpload();

        initUploadButton();
        root.add(addFile);
        form.add(fileUpload);
        root.add(form);

        root.add(attachmentsPanel);

    }

    public void showAttachmentNames() {
        attachmentsPanel.clear();
        for (AttachmentModel model : attachments) {
            addRowWithAttachment(model);
        }
    }


    private void addRowWithAttachment(AttachmentModel model){

        HorizontalPanel horizontalPanel = new HorizontalPanel();

        HorizontalPanel rightSide = new HorizontalPanel();
        HorizontalPanel leftSide = new HorizontalPanel();
        partWidth = widgetWidth/2;
        rightSide.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rightSide.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        rightSide.setWidth(partWidth + "px");
        Label  fileNameLabel = initLabel(model);
        Image deleteImage = null;
        leftSide.add(fileNameLabel);
        deleteImage = createDeleteAttachmentButton(model, horizontalPanel);
        if (model.getId() == null) {

            int imageWidth = deleteImage.getWidth();
            UploaderProgressBar uploaderProgressBar = new UploaderProgressBar((int) (partWidth - imageWidth - 10));
            rightSide.add(uploaderProgressBar);
        }

        rightSide.add(deleteImage);
        horizontalPanel.add(leftSide);
        horizontalPanel.add(rightSide);
        attachmentsPanel.add(horizontalPanel);

    }

    private Image createDeleteAttachmentButton(AttachmentModel model, HorizontalPanel panelToRemove) {

        Image delete = new Image();
        delete.setUrl("cancel.png");
        delete.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);

        DeleteAttachmentsHandler deleteHandler = new DeleteAttachmentsHandler(model, panelToRemove);
        delete.addClickHandler(deleteHandler);
        return delete;
    }

    private void initFileUpload() {
        fileUpload = new FileUpload();
        Style style = fileUpload.getElement().getStyle();
        fileUpload.setName("fileUpload");

        style.setPosition(Style.Position.ABSOLUTE);
        style.setTop(-1000, Style.Unit.PX);
        style.setLeft(-1000, Style.Unit.PX);
        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                form.submit();
            }
        });
    }
    private void initSubmitForm() {
        form = new FormPanel();

        form.setAction("http://127.0.0.1:8080/cm-sochi/attachment-upload");
        // set form to use the POST method, and multipart MIME encoding.
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);
        form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String filePath = event.getResults();
                AttachmentModel model = handleFileNameFromServer(filePath);
                addRowWithAttachment(model);
            }
        });
    }

    private void initUploadButton() {
        addFile = new Image("addButton.png");
        addFile.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fileUpload.getElement().<InputElement>cast().click();

            }
        });
    }

    private AttachmentModel handleFileNameFromServer(String filePath) {
        AttachmentModel attachmentModel = new AttachmentModel();
        String [] splitClearName = filePath.split("-_-");
        String clearName = splitClearName[0];
        attachmentModel.setTemporaryName(filePath);
        attachmentModel.setName(clearName);
        attachments.add(attachmentModel);

        return  attachmentModel;

    }

    private class DeleteAttachmentsHandler implements ClickHandler {

        AttachmentModel model;
        HorizontalPanel panelForRemoving;
        public DeleteAttachmentsHandler(AttachmentModel model, HorizontalPanel panelForRemoving) {
            this.model = model;
            this.panelForRemoving = panelForRemoving;

        }
        @Override
        public void onClick(ClickEvent event) {

            panelForRemoving.removeFromParent();
            attachments.remove(model);

        }
    }
    private Label initLabel(AttachmentModel model) {

        Label fileNameLabel = new Label(model.getName());
        if (model.getId() == null) {
            fileNameLabel.getElement().setId(model.getTemporaryName());
        } else {
            fileNameLabel.getElement().setId(model.getId().toStringRepresentation());
        }

        fileNameLabel.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        fileNameLabel.setWidth(partWidth + "px");

        return fileNameLabel;
    }
}