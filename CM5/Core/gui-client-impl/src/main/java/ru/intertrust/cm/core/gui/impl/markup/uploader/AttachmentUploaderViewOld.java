package ru.intertrust.cm.core.gui.impl.markup.uploader;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
public class AttachmentUploaderViewOld extends Composite {

    private VerticalPanel verticalPanel;
    private Image addFile;
    private FileUpload file;
    private FormPanel form;
    private int widgetWidth;
    private int partWidth;
    private LinkedHashMap<String, Id> fileNameMap;

    public AttachmentUploaderViewOld() {

    }
    public AttachmentUploaderViewOld(LinkedHashMap<String, Id> fileNameMap) {
        this.fileNameMap = fileNameMap;
    }

    public LinkedHashMap<String, Id> getFileNamesMap() {
        return fileNameMap;
    }

    public void setFileNameMap(LinkedHashMap<String, Id> fileNameMap) {
        this.fileNameMap = fileNameMap;
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
        setWidth("100%");

    }

    private void addRow(String fileName){
        if (widgetWidth == 0) {
            widgetWidth = form.getOffsetWidth();
            partWidth = widgetWidth/2;
        }

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        HorizontalPanel rightSide = new HorizontalPanel();
        HorizontalPanel leftSide = new HorizontalPanel();
        rightSide.setWidth(partWidth + "px");
        leftSide.setWidth(partWidth + "px");
        rightSide.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        Label fileNameLabel = new Label(fileName);

        Image deleteImage = createDeleteButton(fileNameLabel);
        int imageWidth = deleteImage.getOffsetWidth();
        UploaderProgressBarOld uploaderProgressBar = new UploaderProgressBarOld((int) (partWidth - 1.1 * imageWidth));
        uploaderProgressBar.update(100);

        fileNameLabel.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        fileNameLabel.setWidth(partWidth + "px");

        leftSide.add(fileNameLabel);
        rightSide.add(uploaderProgressBar);
        rightSide.add(deleteImage);

        horizontalPanel.add(leftSide);
        horizontalPanel.add(rightSide);

        verticalPanel.add(horizontalPanel);

    }
    private Image createDeleteButton(Label label) {

        Image delete = new Image();
        delete.setUrl("cancel.png");
        DeleteHandler deleteHandler = new DeleteHandler(label);
        delete.addClickHandler(deleteHandler);
        return delete;
    }
    private class DeleteHandler implements ClickHandler {

        Label label;
        public DeleteHandler(Label label) {
        this.label = label;

       }
        @Override
        public void onClick(ClickEvent event) {
           String removedName = label.getText();
           fileNameMap.remove(removedName);
           verticalPanel.remove(label.getParent().getParent());

        }
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
                addRow(filePath);
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

}