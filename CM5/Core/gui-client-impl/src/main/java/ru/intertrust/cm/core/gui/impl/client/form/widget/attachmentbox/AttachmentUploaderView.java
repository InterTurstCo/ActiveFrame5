package ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.config.gui.form.widget.AcceptedTypesConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.StyledDialogBox;
import ru.intertrust.cm.core.gui.impl.client.attachment.ExtensionValidator;
import ru.intertrust.cm.core.gui.impl.client.event.UploadCompletedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.UploadUpdatedEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenter.AttachmentElementPresenterFactory;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
public class AttachmentUploaderView extends Composite implements AttachmentElementsContainer {

    private static final BusinessUniverseServiceAsync SERVICE = BusinessUniverseServiceAsync.Impl.getInstance();

    private AbsolutePanel mainBoxPanel;
    private AbsolutePanel root;
    private Style.Display displayStyle;
    private FocusPanel addFile;
    private FileUpload fileUpload;
    private FormPanel submitForm;
    private boolean singleChoice;
    private List<AttachmentItem> attachments = new ArrayList<>();
    private List<AttachmentItem> allAttachments = new ArrayList<>();
    private AcceptedTypesConfig acceptedTypesConfig;
    private ExtensionValidator extensionValidator;
    private Timer elapsedTimer;
    private boolean dontShowNewRow;
    private AttachmentElementPresenterFactory presenterFactory;
    private EventBus eventBus;
    private boolean displayAddButton;

    public AttachmentUploaderView(List<AttachmentItem> attachments, List<AttachmentItem> allAttachments, SelectionStyleConfig selectionStyleConfig,
                                  AcceptedTypesConfig acceptedTypesConfig, boolean displayAddButton,
                                  EventBus eventBus) {
        setAttachments(attachments);
        setAllAttachments(allAttachments);
        this.acceptedTypesConfig = acceptedTypesConfig;
        displayStyle = DisplayStyleBuilder.getDisplayStyle(selectionStyleConfig);
        this.displayAddButton = displayAddButton;
        this.eventBus = eventBus;
        init();
    }

    protected List<AttachmentItem> getAttachments() {
        return attachments;
    }

    protected List<AttachmentItem> getAllAttachments() {
        return allAttachments;
    }

    public void setPresenterFactory(AttachmentElementPresenterFactory presenterFactory) {
        this.presenterFactory = presenterFactory;
    }

    protected boolean isSingleChoice() {
        return singleChoice;
    }

    protected void setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    /**
     * Инициализация частей составного виджета
     */
    private void init() {
        root = new AbsolutePanel();
        root.addStyleName("attachment-plugin-wrapper");
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box linkedWidgetsBorderStyle");
        mainBoxPanel.getElement().getStyle().setDisplay(displayStyle);
        root.add(mainBoxPanel);
        if (displayAddButton) {
            initSubmitForm();
            initFileUpload();
            initUploadButton();
            root.add(addFile);
            submitForm.add(fileUpload);
            root.add(submitForm);
            submitForm.addSubmitCompleteHandler(new FormSubmitCompleteHandler());
            submitForm.addSubmitHandler(new FormSubmitHandler());
        }
        initWidget(root);
    }

    @Override
    public void displayAttachmentItem(AttachmentItem item){
        mainBoxPanel.add(createAttachmentElement(item));
    }

    @Override
    public void displayAttachmentItems(List<AttachmentItem> items) {
        cleanUp();
        setAttachments(items);
        for (Widget element : createSelectedElements()) {
            mainBoxPanel.add(element);
        }
        for (Widget element : createNonSelectedElements()) {
            mainBoxPanel.add(element);
        }
    }

    @Override
    public void displayAttachmentItemInProgress(AttachmentItem item) {
        mainBoxPanel.add(createAttachmentProgressElement(item));
    }

    protected Widget createAttachmentElement(AttachmentItem item) {
        return presenterFactory.createEditablePresenter(item).presentElement();
    }

    protected Widget createAttachmentProgressElement(AttachmentItem item) {
        return presenterFactory.createUploadPresenter(item, new CancelUploadAttachmentHandler(item))
                .presentElement();
    }

    protected List<Widget> createSelectedElements() {
        List<Widget> elements = new ArrayList<>(attachments.size());
        for (AttachmentItem item : attachments) {
           elements.add(createAttachmentElement(item));
        }
        return elements;
    }

    protected List<Widget> createNonSelectedElements() {
        return new ArrayList<>();
    }

    /**
     * Удаляет все прикрепления из отображения
     */
    protected void cleanUp() {
        mainBoxPanel.clear();
    }

    protected void setAttachments(List<AttachmentItem> attachments) {
        this.attachments = attachments;
    }

    protected void setAllAttachments(List<AttachmentItem> allAttachments) {
        this.allAttachments = allAttachments;
    }

    protected void removeAttachment(AttachmentItem attachment) {
        attachments.remove(attachment);
    }

    protected void addAttachment(AttachmentItem attachment) {
        if (singleChoice) {
            attachments.clear();
        }
        attachments.add(attachment);
    }

    protected void clearAttachments() {
        attachments.clear();
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
        extensionValidator = new ExtensionValidator(acceptedTypesConfig);
        extensionValidator.setMimeType(fileUpload.getElement());
      //  fileUpload.getElement().setAttribute("accept", "application/xml");
        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (showRewriteConfirmation()) {
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

    protected boolean showRewriteConfirmation() {
        return singleChoice && !attachments.isEmpty();
    }

    private void initSubmitForm() {

        submitForm = new FormPanel();
        submitForm.setAction(GWT.getHostPageBaseURL() + "attachment-upload");
        // set form to use the POST method, and multipart MIME encoding.
        submitForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        submitForm.setMethod(FormPanel.METHOD_POST);
        submitForm.addStyleName("attachment-plugin-form-panel");
    }

    private void reinitSubmitForm() {
        submitForm.removeFromParent();
        root.add(submitForm);

    }

    private void initUploadButton() {
        addFile = new FocusPanel();
        addFile.addStyleName("attPlImgCreate");
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
                    final StyledDialogBox alert = new StyledDialogBox("Выбранный файл не поддерживается!", true);
                    alert.addOkButtonClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            alert.hide();
                        }
                    });
                    alert.center();
                }
            }
        });

        dialogBox.addCancelButtonClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });

        dialogBox.showDialogBox();
    }

    private void setUpProgressOfUpload(final boolean isUploadCancel) {

        if (isUploadCancel) {
            return;
        }
        SERVICE.getAttachmentUploadPercentage(new AsyncCallback<AttachmentUploadPercentage>() {

            @Override
            public void onFailure(final Throwable t) {
                cancelTimer();
            }

            @Override
            public void onSuccess(AttachmentUploadPercentage percentage) {
                Integer percentageValue = percentage.getPercentage();
                if (percentageValue == 100) {
                    cancelTimer();
                }
                eventBus.fireEvent(new UploadUpdatedEvent(percentageValue));
            }
        });
    }

    private class FormSubmitHandler implements FormPanel.SubmitHandler {

        @Override
        public void onSubmit(FormPanel.SubmitEvent event) {
            String browserFileName = getFileNames(fileUpload.getElement());
            if ("".equals(browserFileName)) {
                return;
            }
            // Let's show single progressBar for all attachments, if multiple files selected.
            // TODO: rework AttachmentUploaderView to be able to show progress separately
            // for (String browserFilename : browserFileNames.split(",")) {
            String filename = getFilename(browserFileName);
            AttachmentItem item = new AttachmentItem();
            item.setName(filename);
            displayAttachmentItemInProgress(item);
            elapsedTimer = new Timer() {
                public void run() {
                    setUpProgressOfUpload(false);
                }
            };
            elapsedTimer.scheduleRepeating(100);
            // }

        }

    }

    private AttachmentItem handleFileNameFromServer(String filePath) {
        AttachmentItem attachmentItem = new AttachmentItem();
        String[] splitClearName = filePath.split("-_-");
        if (splitClearName.length >= 2) {
            String clearName = splitClearName[1];
            attachmentItem.setName(clearName);
        }
        attachmentItem.setTemporaryName(filePath);
        return attachmentItem;
    }

    private class FormSubmitCompleteHandler implements FormPanel.SubmitCompleteHandler {

        public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {

            if (dontShowNewRow) {
                dontShowNewRow = false;
                return;
            }
            String filePaths = event.getResults();

            for (String filePath : filePaths.split(",")) {
                AttachmentItem item = handleFileNameFromServer(filePath);

                addAttachment(item);
                allAttachments.add(item);

                eventBus.fireEvent(new UploadCompletedEvent());
                displayAttachmentItem(item);
                cancelTimer();
            }
        }
    }

    public class CancelUploadAttachmentHandler implements ClickHandler {

        AttachmentItem item;

        public CancelUploadAttachmentHandler(AttachmentItem item) {
            this.item = item;
        }

        @Override
        public void onClick(ClickEvent event) {
            if (item.getTemporaryName() == null && item.getId() == null) {
                dontShowNewRow = true;
                setUpProgressOfUpload(true);
                cancelTimer();
                reinitSubmitForm();
                submitForm.addSubmitCompleteHandler(new FormSubmitCompleteHandler());
            }
        }
    }

    private String getFilename(String browserFilename) {

        if (browserFilename.contains("/")) {
            String[] split = browserFilename.split("/");
            return split[split.length - 1];
        }
        if (browserFilename.contains("\\")) {
            String[] split = browserFilename.split("\\\\");
            return split[split.length - 1];
        }
        return browserFilename;
    }

    private void cancelTimer() {
        if (elapsedTimer != null) {
            elapsedTimer.cancel();
            elapsedTimer = null;
        }
    }

    private static native String getFileNames(Element input) /*-{

        var ret = "";

        //microsoft support
        if (typeof (input.files) == 'undefined'
            || typeof (input.files.length) == 'undefined') {
            return input.value;
        }

        for ( var i = 0; i < input.files.length; i++) {
            if (i > 0) {
                ret += ", ";
            }
            ret += input.files[i].name;
        }
        return ret;
    }-*/;
}