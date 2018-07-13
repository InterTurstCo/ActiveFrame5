package ru.intertrust.cm.core.gui.impl.client.attachment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypesConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ActionLinkConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.StyledDialogBox;
import ru.intertrust.cm.core.gui.impl.client.action.AttachmentActionLinkHandler;
import ru.intertrust.cm.core.gui.impl.client.form.widget.AttachmentBoxWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.DownloadAttachmentHandler;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
@Deprecated
public class AttachmentUploaderView extends Composite {

    private AbsolutePanel mainBoxPanel;
    private AbsolutePanel root;
    private Style.Display displayStyle;
    private Image progressbar;
    private Label percentage;
    private FocusPanel addFile;
    private FileUpload fileUpload;
    private FormPanel submitForm;
    private boolean singleChoice;
    private List<AttachmentItem> attachments;
    private ActionLinkConfig actionLinkConfig;
    private AttachmentBoxWidget owner;
    private AcceptedTypesConfig acceptedTypesConfig;
    private ExtensionValidator extensionValidator;
    public AttachmentUploaderView(SelectionStyleConfig selectionStyleConfig, ActionLinkConfig actionLinkConfig,
                                  AttachmentBoxWidget owner, AcceptedTypesConfig acceptedTypesConfig) {
        this.actionLinkConfig = actionLinkConfig;
        this.owner = owner;
        this.acceptedTypesConfig = acceptedTypesConfig;
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
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

    public void setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    /**
     * Инициализация частей составного виджета
     */
    private void init() {
        root = new AbsolutePanel();
        root.addStyleName("attachmentPluginWrapper");
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box linkedWidgetsBorderStyle");
        mainBoxPanel.getElement().getStyle().setDisplay(displayStyle);
        initSubmitForm();
        initFileUpload();
        initUploadButton();
        root.add(addFile);
        submitForm.add(fileUpload);
        root.add(submitForm);
        submitForm.addStyleName("attachment-plugin-form-panel");
        root.add(mainBoxPanel);
        initWidget(root);
    }

    private Button buildActionLink(ActionLinkConfig actionLinkConfig, final AttachmentBoxWidget widget, final AttachmentItem attachmentItem) {
        Button actionLinkButton = new Button();
        actionLinkButton.removeStyleName("gwt-Button");
        actionLinkButton.addStyleName("dialog-box-button");
        actionLinkButton.setText(actionLinkConfig.getText());

        final String actionName = actionLinkConfig.getActionName();

        actionLinkButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AttachmentActionLinkHandler action = ComponentRegistry.instance.get(actionName);
                action.setAttachmentItem(attachmentItem);
                action.execute();
            }
        });


        return actionLinkButton;
    }

    public void displayAttachmentItem(final AttachmentItem item, AttachmentBoxWidget.CancelUploadAttachmentHandler handler) {
        final AbsolutePanel element = new AbsolutePanel();

        element.setStyleName("facebook-element linkedWidgetsBorderStyle");
        element.addStyleName("loading-attachment");
        Label label = new Label(item.getName());
        label.setStyleName("facebook-label");
        label.addStyleName("facebook-clickable-label");
        progressbar = new Image();
        progressbar.setUrl("CMJSpinner.gif");
        percentage = new Label("0%");
        percentage.addStyleName("loading-attachment");
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
        if (actionLinkConfig != null) {
            element.add(buildActionLink(actionLinkConfig, owner, item));
        }
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
        if (actionLinkConfig != null) {
            element.add(buildActionLink(actionLinkConfig, owner, item));
        }
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
        if (!singleChoice) {
            fileUpload.getElement().setAttribute("multiple", "multiple");
        }
        Style style = fileUpload.getElement().getStyle();
        fileUpload.setName("fileUpload");

        style.setPosition(Style.Position.ABSOLUTE);
        style.setTop(-1000, Style.Unit.PX);
        style.setLeft(-1000, Style.Unit.PX);
        extensionValidator = new ExtensionValidator(acceptedTypesConfig, false);
        extensionValidator.setMimeType(fileUpload.getElement());
      //  fileUpload.getElement().setAttribute("accept", "application/xml");
        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (singleChoice && !attachments.isEmpty()) {
                    showDialogBox();
                } else {
                    InputElement inputElement = fileUpload.getElement().cast();
                    String filename = inputElement.getValue();
                    if(filename.length() > 0 && extensionValidator.isFilesExtensionValid(filename)) {
                        submitForm.submit();
                    } else {
                        final StyledDialogBox alert = new StyledDialogBox("Выбраный файл не поддерживается!", true);
                        alert.addOkButtonClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                alert.hide();
                            }
                        });
                        alert.center();
                    }
                }

            }
        });

    }

    private void initSubmitForm() {

        submitForm = new FormPanel();
        submitForm.setAction((GWT.getHostPageBaseURL() + "attachment-upload"));
        // set form to use the POST method, and multipart MIME encoding.
        submitForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        submitForm.setMethod(FormPanel.METHOD_POST);

    }

    public void reinitSubmitForm() {
        submitForm.removeFromParent();
        root.add(submitForm);

    }

    private void initUploadButton() {
        addFile = new FocusPanel();
        addFile.addStyleName("lightButton uploadButton");
        Style style = addFile.getElement().getStyle();
        addFile.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fileUpload.getElement().<InputElement>cast().click();

            }
        });
    }

    private void showDialogBox() {
        final StyledDialogBox dialogBox = new StyledDialogBox("Текущее вложение будет перезаписано. \nПродолжить?");
        dialogBox.addOkButtonClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dialogBox.hide();
                mainBoxPanel.clear();
                attachments.clear();
                InputElement inputElement = fileUpload.getElement().cast();
                String fileNames = inputElement.getValue();
                if(fileNames.length() > 0 && extensionValidator.isFilesExtensionValid(fileNames)) {
                    submitForm.submit();
                }   else {
                final StyledDialogBox alert = new StyledDialogBox("Выбраный файл не поддерживается!", true);
                alert.addOkButtonClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        alert.hide();
                    }
                });
                alert.center();
            }

               /* */

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