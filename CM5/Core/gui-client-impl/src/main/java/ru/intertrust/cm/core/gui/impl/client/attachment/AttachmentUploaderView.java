package ru.intertrust.cm.core.gui.impl.client.attachment;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
public class AttachmentUploaderView extends Composite {

    private VerticalPanel verticalPanel;
    private Image addFile;
    private FileUpload file;
    private FormPanel form;
    private int widgetWidth;
    private int partWidth;
    private LinkedHashMap<Id, AttachmentModel> savedAttachments;
    private List<AttachmentModel> newAttachments;

     public  AttachmentUploaderView() {

    }
    public AttachmentUploaderView(LinkedHashMap<Id, AttachmentModel> attachments) {
        this.savedAttachments = attachments;
         newAttachments = new ArrayList<AttachmentModel>();
        init();
    }

    public LinkedHashMap<Id, AttachmentModel> getSavedAttachments() {
        return savedAttachments;
    }

    public void setSavedAttachments(LinkedHashMap<Id, AttachmentModel> savedAttachments) {
        this.savedAttachments = savedAttachments;
    }

    public List<AttachmentModel> getNewAttachments() {
        return newAttachments;
    }

    public void setNewAttachments(List<AttachmentModel> newAttachments) {
        this.newAttachments = newAttachments;
    }

    public void init() {
        initSubmitForm();
        this.initWidget(form);
        verticalPanel = new VerticalPanel();
        initFileUpload();

        initUploadButton();

        verticalPanel.add(addFile);
        verticalPanel.add(file);
        form.add(verticalPanel);


    }
    public void showAttachmentNames() {
        for (AttachmentModel model : savedAttachments.values()) {
            String fileName = model.getName();
            addRowWithAttachmentFileName(fileName);
        }
    }

    private void addRowWithAttachmentFileName(String fileName){
        if (widgetWidth == 0) {
            widgetWidth = form.getOffsetWidth();
            widgetWidth = widgetWidth == 0 ? 700 : widgetWidth;
            partWidth = widgetWidth/2;
            System.out.println("widget width " + widgetWidth);
            System.out.println("part width " + partWidth);
        }

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        HorizontalPanel rightSide = new HorizontalPanel();
        HorizontalPanel leftSide = new HorizontalPanel();
        rightSide.setWidth(partWidth + "px");
        leftSide.setWidth(partWidth + "px");
        rightSide.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rightSide.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        Label fileNameLabel = new Label(fileName);

        Image deleteImage = createDeleteButton(fileNameLabel);
        int imageWidth = deleteImage.getOffsetWidth();
        UploaderProgressBar uploaderProgressBar = new UploaderProgressBar((int) (partWidth - 1.1 * imageWidth));
        //uploaderProgressBar.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);

        fileNameLabel.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        fileNameLabel.setWidth(partWidth + "px");

        leftSide.add(fileNameLabel);
        rightSide.add(uploaderProgressBar);
        rightSide.add(deleteImage);

        horizontalPanel.add(leftSide);
        horizontalPanel.add(rightSide);

        uploaderProgressBar.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
        verticalPanel.add(horizontalPanel);

    }
    private void addRowWithNewUploadedFileName(AttachmentModel attachmentModel){
        if (widgetWidth == 0) {
            widgetWidth = form.getOffsetWidth();
            widgetWidth = widgetWidth == 0 ? 700 : widgetWidth;
            partWidth = widgetWidth/2;
            System.out.println("widget width " + widgetWidth);
            System.out.println("part width " + partWidth);
        }

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        HorizontalPanel rightSide = new HorizontalPanel();
        HorizontalPanel leftSide = new HorizontalPanel();
        rightSide.setWidth(partWidth + "px");
        leftSide.setWidth(partWidth + "px");
        rightSide.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rightSide.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        Label fileNameLabel = new Label(attachmentModel.getName());
        fileNameLabel.getElement().setId(attachmentModel.getPath());

        Image deleteImage = createDeleteButton(fileNameLabel);
        int imageWidth = deleteImage.getOffsetWidth();
        UploaderProgressBar uploaderProgressBar = new UploaderProgressBar((int) (partWidth - 1.1 * imageWidth));
        //uploaderProgressBar.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);

        fileNameLabel.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        fileNameLabel.setWidth(partWidth + "px");

        leftSide.add(fileNameLabel);
        rightSide.add(uploaderProgressBar);
        rightSide.add(deleteImage);

        horizontalPanel.add(leftSide);
        horizontalPanel.add(rightSide);

        uploaderProgressBar.update(50);
        verticalPanel.add(horizontalPanel);

    }
    private Image createDeleteButton(Label label) {

        Image delete = new Image();
        delete.setUrl("cancel.png");
        delete.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);

        DeleteHandler deleteHandler = new DeleteHandler(label);
        delete.addClickHandler(deleteHandler);
        return delete;
    }

    private void initFileUpload() {
        file = new FileUpload();
        Style style = file.getElement().getStyle();
        file.setName("file");

        style.setPosition(Style.Position.ABSOLUTE);
        style.setTop(-1000, Style.Unit.PX);
        style.setLeft(-1000, Style.Unit.PX);
        file.addChangeHandler(new ChangeHandler() {
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
                addRowWithNewUploadedFileName(model);
            }
        });
    }

    private void initUploadButton() {
        addFile = new Image("addButton.png");
        addFile.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                file.getElement().<InputElement>cast().click();

            }
        });
    }

    private AttachmentModel handleFileNameFromServer(String filePath) {
        AttachmentModel attachmentModel = new AttachmentModel();
        String [] splitBySeparatorLinux = filePath.split("/");
        int length = splitBySeparatorLinux.length;

        if (length != 1) {
             String rawName = splitBySeparatorLinux[length-1];
             String [] splitClearName = rawName.split("-_-");
             String clearName = splitClearName[0];

             attachmentModel.setName(clearName);
             attachmentModel.setPath(filePath);
             newAttachments.add(attachmentModel);
             return  attachmentModel;
        }
        String [] splitBySeparatorWindows = filePath.split("\\\\");
        length = splitBySeparatorWindows.length;
        String rawName = splitBySeparatorWindows[length-1];
        String [] splitClearName = rawName.split("-_-");
        String clearName = splitClearName[0];

        attachmentModel.setName(clearName);
        attachmentModel.setPath(filePath);
        newAttachments.add(attachmentModel);
        return  attachmentModel;
    }

    private class DeleteHandler implements ClickHandler {

        Label label;
        public DeleteHandler(Label label) {
            this.label = label;

        }
        @Override
        public void onClick(ClickEvent event) {
            String removedId = label.getElement().getId();
            verticalPanel.remove(label.getParent().getParent());

            for (AttachmentModel attachmentModel : savedAttachments.values()) {
                if (removedId.equals(attachmentModel.getPath())) {
                    savedAttachments.values().remove(attachmentModel);
                    return;
                }
            }
            for (AttachmentModel attachmentModel : newAttachments) {
                if (removedId.equals(attachmentModel.getPath())) {
                    newAttachments.remove(attachmentModel);
                    return;
                }
            }

        }
    }
}