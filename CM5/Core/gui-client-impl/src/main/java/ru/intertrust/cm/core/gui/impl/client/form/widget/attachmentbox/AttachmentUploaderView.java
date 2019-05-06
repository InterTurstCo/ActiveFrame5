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
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.AttachmentUploadPercentage;
import ru.intertrust.cm.core.config.gui.form.widget.AddButtonConfig;
import ru.intertrust.cm.core.config.gui.form.widget.ClearAllButtonConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.impl.client.ApplicationWindow;
import ru.intertrust.cm.core.gui.impl.client.StyledDialogBox;
import ru.intertrust.cm.core.gui.impl.client.attachment.ExtensionValidator;
import ru.intertrust.cm.core.gui.impl.client.event.UploadCompletedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.UploadUpdatedEvent;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.AttachmentElementPresenterFactory;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.EditablePresenterFactory;
import ru.intertrust.cm.core.gui.impl.client.form.widget.attachmentbox.presenterFactory.UploadProgressPresenterFactory;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonForm;
import ru.intertrust.cm.core.gui.impl.client.util.DisplayStyleBuilder;
import ru.intertrust.cm.core.gui.impl.server.widget.AttachmentUploaderServlet;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
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

  private Panel mainBoxPanel;
  private Panel controlPanel;
  private Panel root;
  private Style.Display displayStyle;
  private FocusPanel addFile;
  private FocusPanel clearAllButton;
  private FileUpload fileUpload;
  private FormPanel submitForm;

  private List<AttachmentItem> attachments = new ArrayList<>();
  private List<AttachmentItem> allAttachments = new ArrayList<>();

  private ExtensionValidator extensionValidator;
  private Timer elapsedTimer;
  private boolean dontShowNewRow;
  private AttachmentElementPresenterFactory presenterFactory;
  private AttachmentElementPresenterFactory uploadPresenterFactory;
  private EventBus eventBus;
  private boolean singleChoice;
  private AddButtonConfig addButtonConfig;
  private ClearAllButtonConfig clearAllButtonConfig;
  private AttachmentBoxState state;

  private BaseWidget parent;

  public AttachmentUploaderView(AttachmentBoxState state, EventBus eventBus, BaseWidget parent) {
    this.state = state;
    setAttachments(state.getAttachments());
    setAllAttachments(state.getAllAttachments());
    this.extensionValidator = new ExtensionValidator(state.getAcceptedTypesConfig(), state.getImagesConfig() != null);
    displayStyle = DisplayStyleBuilder.getDisplayStyle(state.getSelectionStyleConfig());
    this.addButtonConfig = state.getAddButtonConfig();
    this.clearAllButtonConfig = state.getClearAllButtonConfig();
    this.singleChoice = state.isSingleChoice();
    this.eventBus = eventBus;
    presenterFactory = new EditablePresenterFactory(state.getActionLinkConfig(),
        state.getImagesConfig(), state.getDeleteButtonConfig(), state.getDigitalSignaturesConfig());
    uploadPresenterFactory = new UploadProgressPresenterFactory(state.getActionLinkConfig(),
        state.getDeleteButtonConfig(), eventBus);
    this.parent = parent;
    init();
  }

  protected List<AttachmentItem> getAttachments() {
    return attachments;
  }

  protected List<AttachmentItem> getAllAttachments() {
    return allAttachments;
  }

  protected boolean isSingleChoice() {
    return singleChoice;
  }

  protected Panel getAttachmentsPanel() {
    return mainBoxPanel;
  }

  protected Panel getControlPanel() {
    return controlPanel;
  }

  /**
   * Инициализация частей составного виджета
   */
  private void init() {
    root = new AbsolutePanel();
    root.addStyleName("attachmentPluginWrapper");

    initControlPanel();
    initAttachmentsPanel();

    initWidget(root);
  }

  private void initAttachmentsPanel() {
    mainBoxPanel = new AbsolutePanel();
    mainBoxPanel.setStyleName("facebook-main-box");
    mainBoxPanel.addStyleName(attachments.isEmpty() ? "linkedWidgetsBorderNone" : "linkedWidgetsBorderStyle");
    mainBoxPanel.getElement().getStyle().setDisplay(displayStyle);
    root.add(mainBoxPanel);
  }

  private void initControlPanel() {
    controlPanel = new AbsolutePanel();
    controlPanel.getElement().setClassName("attachmentPluginButtonsPanel");
    root.add(controlPanel);
    if (clearAllButtonConfig != null && clearAllButtonConfig.isDisplay()) {
      initClearAllButton();
      controlPanel.add(clearAllButton);
    }
    if (addButtonConfig == null || addButtonConfig.isDisplay()) {
      initSubmitForm();
      initFileUpload();
      initUploadButton();
      controlPanel.add(addFile);
      submitForm.add(fileUpload);
      controlPanel.add(submitForm);
      submitForm.addSubmitCompleteHandler(new FormSubmitCompleteHandler());
      submitForm.addSubmitHandler(new FormSubmitHandler());
    }
  }

  @Override
  public void displayAttachmentItems() {
    cleanUp();
    for (Widget element : createSelectedElements()) {
      mainBoxPanel.add(element);
    }
    mainBoxPanel.setStyleName("facebook-main-box");
    mainBoxPanel.addStyleName(attachments.isEmpty() ? "linkedWidgetsBorderNone" : "linkedWidgetsBorderStyle");
  }

  private void displaySelectedElement(AttachmentItem item) {
    getAttachmentsPanel().add(createSelectedElement(item));
  }

  private void displayAttachmentItemInProgress(AttachmentItem item) {
    mainBoxPanel.add(createAttachmentProgressElement(item));
  }

  protected Panel createSelectedElement(AttachmentItem item) {
    return presenterFactory.createPresenter(item, new DeleteAttachmentClickHandler(item), getAttachments()).presentElement();
  }

  protected Panel createNonSelectedElement(AttachmentItem item) {
    return presenterFactory.createPresenter(item, new DeleteAttachmentClickHandler(item), getAttachments()).presentElement();
  }

  protected Panel createAttachmentProgressElement(AttachmentItem item) {
    return uploadPresenterFactory.createPresenter(item, new CancelUploadAttachmentHandler(item), getAttachments())
        .presentElement();
  }

  protected boolean isItemSelected(AttachmentItem item) {
    return attachments.contains(item);
  }

  protected List<Widget> createSelectedElements() {
    List<Widget> elements = new ArrayList<>(attachments.size());
    for (AttachmentItem item : attachments) {
      elements.add(createSelectedElement(item));
    }
    return elements;
  }

  /**
   * Удаляет все прикрепления из отображения
   */
  protected void cleanUp() {
    getAttachmentsPanel().clear();
    mainBoxPanel.setStyleName("facebook-main-box linkedWidgetsBorderNone");
  }

  protected void setAttachments(List<AttachmentItem> attachments) {
    this.attachments = attachments;
  }

  protected void setAllAttachments(List<AttachmentItem> allAttachments) {
    this.allAttachments = allAttachments;
  }

  protected void selectAttachment(AttachmentItem attachment) {
    if (singleChoice) {
      attachments.clear();
    }
    if (!attachments.contains((attachment))) {
      attachments.add(attachment);
    }
    parent.validate();
  }

  protected void deselectAttachment(AttachmentItem attachment) {
    attachments.remove(attachment);
    parent.validate();
  }

  protected void addAttachment(AttachmentItem attachment) {
    if (!allAttachments.contains(attachment)) {
      allAttachments.add(attachment);
    }
    selectAttachment(attachment);
  }

  protected void removeAttachment(AttachmentItem attachment) {
    deselectAttachment(attachment);
    allAttachments.remove(attachment);
  }

  protected void deselectAllAttachments() {
    attachments.clear();
    parent.validate();
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
          if (filename.length() > 0 && extensionValidator.isFilesExtensionValid(filename)) {
            submitForm.submit();
            Application.getInstance().setInUploadProcess(true);
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
    if (state.getAppname() != null) {
      submitForm.setAction((GWT.getHostPageBaseURL() + "attachment-upload").replace("/" + state.getAppname(), ""));
    } else {
      submitForm.setAction(GWT.getHostPageBaseURL() + "attachment-upload");
    }
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
    addFile.addStyleName("lightButton uploadButton");
    addFile.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        fileUpload.getElement().<InputElement>cast().click();

      }
    });
  }

  private void initClearAllButton() {
    clearAllButton = new FocusPanel();
    // clearAllButton.addStyleName("lightButton");
    ButtonForm buttonForm = new ButtonForm(clearAllButton, clearAllButtonConfig.getImage(), clearAllButtonConfig.getText());
    clearAllButton.add(buttonForm);
    clearAllButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!attachments.isEmpty()) {
          ApplicationWindow.confirm("Очистить содержимое?", new ConfirmCallback() {
            @Override
            public void onAffirmative() {
              cleanUp();
              deselectAllAttachments();
            }

            @Override
            public void onCancel() {
              //do nothing
            }
          });
        }
      }
    });
  }

  private void showDialogBox() {
    final StyledDialogBox dialogBox = new StyledDialogBox("Текущее вложение будет перезаписано. \nПродолжить?");
    dialogBox.addOkButtonClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        cleanUp();
        attachments.clear();
        InputElement inputElement = fileUpload.getElement().cast();
        String fileNames = inputElement.getValue();
        if (fileNames.length() > 0 && extensionValidator.isFilesExtensionValid(fileNames)) {
          submitForm.submit();
          Application.getInstance().setInUploadProcess(true);
        } else {
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
    String[] splitClearName = filePath.split(AttachmentUploaderServlet.FILE_NAME_PARAMS_DELIMITER);

    if (splitClearName.length >= 2) {
      String clearName = splitClearName[1];
      attachmentItem.setName(clearName);

      if (splitClearName.length >= 3) {
        final String contentLength = splitClearName[2];
        attachmentItem.setContentLength(contentLength);
      }
    }

    attachmentItem.setTemporaryName(filePath);
    return attachmentItem;
  }

  private class FormSubmitCompleteHandler implements FormPanel.SubmitCompleteHandler {

    public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {

      if (dontShowNewRow) {
        dontShowNewRow = false;
        Application.getInstance().setInUploadProcess(false);
        return;
      }
      String filePaths = event.getResults();

      for (String filePath : filePaths.split("\\*")) {
        AttachmentItem item = handleFileNameFromServer(filePath);

        addAttachment(item);
        displaySelectedElement(item);
        eventBus.fireEvent(new UploadCompletedEvent()); //TODO: why we do it in loop?
        cancelTimer();
      }
      mainBoxPanel.setStyleName("facebook-main-box linkedWidgetsBorderStyle");
      submitForm.reset();
      Application.getInstance().setInUploadProcess(false);
    }
  }

  public class DeleteAttachmentClickHandler implements ClickHandler {

    private final AttachmentItem item;

    DeleteAttachmentClickHandler(AttachmentItem item) {
      this.item = item;
    }

    @Override
    public void onClick(ClickEvent event) {
      removeAttachment(item);
      mainBoxPanel.setStyleName("facebook-main-box");
      mainBoxPanel.addStyleName(attachments.isEmpty() ? "linkedWidgetsBorderNone" : "linkedWidgetsBorderStyle");
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
//                reinitSubmitForm();
//               submitForm.addSubmitCompleteHandler(new FormSubmitCompleteHandler());
        submitForm.reset();
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

      for (var i = 0; i < input.files.length; i++) {
          if (i > 0) {
              ret += "* ";
          }
          ret += input.files[i].name;
      }
      return ret;
  }-*/;
}