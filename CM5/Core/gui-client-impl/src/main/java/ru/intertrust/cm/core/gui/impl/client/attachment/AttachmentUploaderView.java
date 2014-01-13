package ru.intertrust.cm.core.gui.impl.client.attachment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.impl.client.StyledDialogBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.AttachmentBoxWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DownloadAttachmentHandler;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
public class AttachmentUploaderView extends Composite {
    private static final String DISPLAY_STYLE_INLINE = "inline";
    private static final String DISPLAY_STYLE_TABLE = "table";
    private AbsolutePanel mainBoxPanel;
    private AbsolutePanel root;
    private Style.Display displayStyle;
    private String selectionStyle;
    private Image progressbar;
    private Label percentage;
    private Image addFile;
    private FileUpload fileUpload;
    private FormPanel submitForm;
    private boolean singleChoice;
    private List<AttachmentItem> attachments;

    public AttachmentUploaderView() {
        init();
    }

    /**
     * Возвращает сохраненные и несохраненные прикрепления
     *
     * @param attachments
     */
    public AttachmentUploaderView(List<AttachmentItem> attachments) {
        this.attachments = attachments;
        init();
    }

    public FormPanel getSubmitForm() {
        return submitForm;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public List<AttachmentItem> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentItem> attachments) {
        this.attachments = attachments;
    }

    public void addFormSubmitCompleteHandler(FormPanel.SubmitCompleteHandler submitCompleteHandler) {
        submitForm.addSubmitCompleteHandler(submitCompleteHandler);
    }

    public void addFormSubmitHandler(FormPanel.SubmitHandler submitHandler) {
        submitForm.addSubmitHandler(submitHandler);
    }

    public Label getPercentage() {
        return percentage;
    }

    public Image getProgressbar() {
        return progressbar;
    }

    public String getSelectionStyle() {
        return selectionStyle;
    }

    public void setSelectionStyle(String selectionStyle) {
        this.selectionStyle = selectionStyle;
    }

    public void setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    /**
     * Инициализация частей составного виджета
     */
    private void init() {
        root = new AbsolutePanel();
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box");

        initSubmitForm();
        initFileUpload();
        initUploadButton();
        root.add(addFile);
        submitForm.add(fileUpload);
        root.add(submitForm);
        root.add(mainBoxPanel);
        initWidget(root);
    }

    public void displayAttachmentItem(final AttachmentItem item, AttachmentBoxWidget.CancelUploadAttachmentHandler handler) {
        final AbsolutePanel element = new AbsolutePanel();

        element.setStyleName("facebook-element");
        Label label = new Label(item.getName());
        label.setStyleName("facebook-label");
        progressbar = new Image();
        progressbar.setUrl("CMJSpinner.gif");
        percentage = new Label("0%");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.getElement().getStyle().setPadding(2, Style.Unit.PX);
        delBtn.getElement().getStyle().setBackgroundColor("red");

        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                attachments.remove(item);
                element.removeFromParent();
            }
        });
        delBtn.addClickHandler(handler);
        element.add(label);
        element.add(progressbar);
        element.add(percentage);
        element.add(delBtn);
        mainBoxPanel.add(element);
    }

    public void displayAttachmentLinkItem(final AttachmentItem item) {
        final AbsolutePanel element = new AbsolutePanel();

        element.setStyleName("facebook-element");
        String contentLength = item.getContentLength();

        String anchorTitle = contentLength == null ? item.getName() : item.getName() + " (" + item.getContentLength() + ")";
        Anchor fileNameAnchor = new Anchor(anchorTitle);
        fileNameAnchor.addClickHandler(new DownloadAttachmentHandler(item));
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.getElement().getStyle().setPadding(2, Style.Unit.PX);
        delBtn.getElement().getStyle().setBackgroundColor("red");

        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                attachments.remove(item);
                element.removeFromParent();
            }
        });
        element.add(fileNameAnchor);
        element.add(delBtn);
        mainBoxPanel.add(element);
    }

    /**
     * Удаляет все прикрепления из отображения
     */
    public void cleanUp() {
        mainBoxPanel.clear();
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
              if (singleChoice && !attachments.isEmpty()) {
                  showDialogBox();
              } else {
                  submitForm.submit();
                  InputElement inputElement = fileUpload.getElement().cast();
                  inputElement.setValue("");
              }

            }
        });

    }

    private void initSubmitForm() {

        submitForm = new FormPanel();
        submitForm.setAction(GWT.getHostPageBaseURL() + "attachment-upload");
        // set form to use the POST method, and multipart MIME encoding.
        submitForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        submitForm.setMethod(FormPanel.METHOD_POST);

    }

    public void reinitSubmitForm() {
        submitForm.removeFromParent();
        root.add(submitForm);

    }

    private void initUploadButton() {
        addFile = new Image("icons/icon-create.png");
        Style style = addFile.getElement().getStyle();
        style.setMarginBottom(10, Style.Unit.PX);
        addFile.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fileUpload.getElement().<InputElement>cast().click();

            }
        });
    }

    public void initDisplayStyle(String howToDisplay) {

        if (DISPLAY_STYLE_INLINE.equalsIgnoreCase(howToDisplay)) {
            displayStyle = Style.Display.INLINE_BLOCK;
        }
        if (DISPLAY_STYLE_TABLE.equalsIgnoreCase(howToDisplay)) {
            displayStyle = Style.Display.BLOCK;
        }

        mainBoxPanel.getElement().getStyle().setDisplay(displayStyle);
    }

    private void showDialogBox() {
       final StyledDialogBox dialogBox = new StyledDialogBox("Текущее прикрпление будет перезаписано. \nПродолжить?");
        dialogBox.addOkButtonClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                mainBoxPanel.clear();
                attachments.clear();
                submitForm.submit();
                InputElement inputElement = fileUpload.getElement().cast();
                inputElement.setValue("");
                dialogBox.hide();
            }
        });

        dialogBox.addCancelButtonClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });

        dialogBox.showDialogBox();
    }
}