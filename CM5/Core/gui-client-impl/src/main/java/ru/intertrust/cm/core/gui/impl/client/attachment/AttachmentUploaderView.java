package ru.intertrust.cm.core.gui.impl.client.attachment;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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

    public VerticalPanel getAttachmentsPanel() {
        return attachmentsPanel;
    }

    public void setAttachmentsPanel(VerticalPanel attachmentsPanel) {
        this.attachmentsPanel = attachmentsPanel;
    }

    public HandlerRegistration addFormSubmitCompleteHandler(FormPanel.SubmitCompleteHandler submitCompleteHandler){
        return form.addSubmitCompleteHandler(submitCompleteHandler);
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

    public void addRowWithAttachment(AttachmentModel model,Image deleteAttachment, Anchor anchor, String contentLength){

        HorizontalPanel horizontalPanel = new HorizontalPanel();

        HorizontalPanel rightSide = new HorizontalPanel();
        HorizontalPanel leftSide = new HorizontalPanel();
        partWidth = widgetWidth/2;
        rightSide.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rightSide.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        rightSide.setWidth(partWidth + "px");
        leftSide.setWidth(partWidth + "px");
        if (model.getId() == null) {
            Label  fileNameLabel = initLabel(model);
            leftSide.add(fileNameLabel);
            int imageWidth = deleteAttachment.getWidth();
            UploaderProgressBar uploaderProgressBar = new UploaderProgressBar(partWidth - imageWidth - 10);
            rightSide.add(uploaderProgressBar);
        } else {
            leftSide.add(anchor);
            Label contentLengthLabel = new Label(contentLength);
            contentLengthLabel.getElement().getStyle().setFloat(Style.Float.RIGHT);
            leftSide.add(contentLengthLabel);
            leftSide.getElement().getStyle().setMarginLeft(20, Style.Unit.PX);
        }

        rightSide.add(deleteAttachment);
        horizontalPanel.add(leftSide);
        horizontalPanel.add(rightSide);
        attachmentsPanel.add(horizontalPanel);

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

    }

    private void initUploadButton() {
        addFile = new Image("add-button.png");
        Style style = addFile.getElement().getStyle();
        style.setMarginBottom(10, Style.Unit.PX);

        addFile.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fileUpload.getElement().<InputElement>cast().click();

            }
        });
    }

    private Label initLabel(AttachmentModel model) {

        Label fileNameLabel = new Label(model.getName());
        Style style = fileNameLabel.getElement().getStyle();
        style.setOverflow(Style.Overflow.HIDDEN);
        style.setFontSize(98, Style.Unit.PCT);

        return fileNameLabel;
    }

}