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
    private FormPanel submitForm;
    private int widgetWidth;
    private int partWidth;
    private UploaderProgressBar uploaderProgressBar;
    private List<AttachmentModel> attachments;
    private VerticalPanel root;

    public AttachmentUploaderView() {
        init();
    }

    /**
     * Возвращает сохраненные и несохраненные прикрепления
     *
     * @param attachments
     */
    public AttachmentUploaderView(List<AttachmentModel> attachments) {
        this.attachments = attachments;
        init();
    }

    public UploaderProgressBar getUploaderProgressBar() {
        return uploaderProgressBar;
    }


    public FormPanel getSubmitForm() {
        return submitForm;
    }

    public void adjustWidgetSizes(int widgetWidth) {
        this.widgetWidth = widgetWidth;
        partWidth = widgetWidth / 2;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public List<AttachmentModel> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentModel> attachments) {
        this.attachments = attachments;
    }

    public void addFormSubmitCompleteHandler(FormPanel.SubmitCompleteHandler submitCompleteHandler) {
        submitForm.addSubmitCompleteHandler(submitCompleteHandler);
    }

    public void addFormSubmitHandler(FormPanel.SubmitHandler submitHandler) {
        submitForm.addSubmitHandler(submitHandler);
    }

    /**
     * Инициализация частей составного виджета
     */
    private void init() {
        root = new VerticalPanel();

        attachmentsPanel = new VerticalPanel();
        initSubmitForm();
        initFileUpload();

        initUploadButton();
        root.add(addFile);
        submitForm.add(fileUpload);

        root.add(submitForm);
        root.add(attachmentsPanel);
        initWidget(root);
    }

    /**
     * Cоздает строку-отображение состояния выгрузки прикрекпления
     *
     * @param model        модель прикрепления
     * @param deleteButton кнопка для удаления отображения и отмены выгрузки
     * @param anchor       линк для скачивания прикрепления, в случаи еще не сохраненного прикрепления - null
     */
    public void addRowWithAttachment(AttachmentModel model, Image deleteButton, Anchor anchor) {

        HorizontalPanel horizontalPanel = new HorizontalPanel();

        HorizontalPanel rightSide = new HorizontalPanel();
        HorizontalPanel leftSide = new HorizontalPanel();

        rightSide.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        rightSide.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        rightSide.setWidth(partWidth + "px");
        leftSide.setWidth(partWidth + "px");
        Image stylizedDeleteButton = stylizeDeleteButton(deleteButton);
        if (model.getId() == null) {
            Label fileNameLabel = initLabel(model);
            leftSide.add(fileNameLabel);
            int imageWidth = stylizedDeleteButton.getWidth();
            uploaderProgressBar = new UploaderProgressBar(partWidth - imageWidth - 10);
            rightSide.add(uploaderProgressBar);

        } else {

            leftSide.add(stylizeAnchor(anchor));
        }

        rightSide.add(stylizedDeleteButton);
        horizontalPanel.add(leftSide);
        horizontalPanel.add(rightSide);
        attachmentsPanel.add(horizontalPanel);

    }

    /**
     * Удаляет все прикрепления из отображения
     */
    public void cleanUp() {
        attachmentsPanel.clear();
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
                submitForm.submit();
            }
        });
    }

    private void initSubmitForm() {
        submitForm = new FormPanel();

        submitForm.setAction("http://127.0.0.1:8080/cm-sochi/attachment-upload");
        // set form to use the POST method, and multipart MIME encoding.
        submitForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        submitForm.setMethod(FormPanel.METHOD_POST);


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

    private Anchor stylizeAnchor(Anchor fileNameAnchor) {

        Style style = fileNameAnchor.getElement().getStyle();
        style.setOverflow(Style.Overflow.HIDDEN);
        style.setMarginLeft(20, Style.Unit.PX);
        fileNameAnchor.setWidth(partWidth + "px");

        return fileNameAnchor;
    }

    private Image stylizeDeleteButton(Image deleteButton) {
        deleteButton.setUrl("button_cancel.png");
        deleteButton.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);

        return deleteButton;
    }

}