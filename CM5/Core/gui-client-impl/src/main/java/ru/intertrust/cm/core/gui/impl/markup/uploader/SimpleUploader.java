package ru.intertrust.cm.core.gui.impl.markup.uploader;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 21.10.13
 *         Time: 13:15
 */
public class SimpleUploader extends Composite {

    private VerticalPanel verticalPanel;
    private Image addFile;
    private FileUpload file;
    private FormPanel form;
    private int widgetWidth;
    private int partWidth;

    public SimpleUploader() {

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
        Image deleteImage = createDeleteButton();
        int imageWidth = deleteImage.getOffsetWidth();
        UploaderProgressBar uploaderProgressBar = new UploaderProgressBar((int) (partWidth - 1.1 * imageWidth));
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
    private Image createDeleteButton() {

        Image delete = new Image();
        delete.setUrl("cancel.png");
        DeleteHandler deleteHandler = new DeleteHandler(delete);
        delete.addClickHandler(deleteHandler);
        return delete;
    }
    private class DeleteHandler implements ClickHandler {
        Image delete;
        public DeleteHandler(Image delete) {
        this.delete = delete;

       }
        @Override
        public void onClick(ClickEvent event) {
          verticalPanel.remove(delete.getParent().getParent());

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
                  String filename = file.getFilename();

                  addRow(filename);
                  form.submit();
              }
          });
      }
     private void initSubmitForm() {
         form = new FormPanel();
         form.setAction("http://www.tutorialspoint.com/gwt/myFormHandler");
         // set form to use the POST method, and multipart MIME encoding.
         form.setEncoding(FormPanel.ENCODING_MULTIPART);
         form.setMethod(FormPanel.METHOD_POST);
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