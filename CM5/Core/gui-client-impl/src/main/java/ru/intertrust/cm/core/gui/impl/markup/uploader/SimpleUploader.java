package ru.intertrust.cm.core.gui.impl.markup.uploader;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 18.10.13
 * Time: 18:13
 * To change this template use File | Settings | File Templates.
 */
public class SimpleUploader extends Composite {
    VerticalPanel verticalPanel;
    Image addFile;
    int widgetWidth = 0;
    int partWidth =0;
    FileUpload file;

    public SimpleUploader() {

    }

    public void init() {
        verticalPanel = new VerticalPanel();
        this.initWidget(verticalPanel);
        initFileUpload();

        addFile = new Image("addButton.png");
        addFile.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                file.getElement().<InputElement>cast().click();

            }
        });
        verticalPanel.add(addFile);
        verticalPanel.add(file);


        setWidth("100%");
    }



    private void addRow(String fileName){
        if (widgetWidth == 0) {
            widgetWidth = verticalPanel.getOffsetWidth();
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
      //  fileNameLabel.setHeight("20px");
        leftSide.add(fileNameLabel);
        rightSide.add(uploaderProgressBar);
        rightSide.add(deleteImage);

        horizontalPanel.add(leftSide);
        horizontalPanel.add(rightSide);

        verticalPanel.add(horizontalPanel);


   /*     if  (textSize >  width/2) {
            int offset = textSize - width;
            int numberOfChars = fileName.length();
            System.out.println("works " );
            String rawText = fileName.substring(0, numberOfChars - offset);
            String truncatedFilename = rawText + "...";
            System.out.println(verticalPanel.getWidgetIndex(leftSide));
            System.out.println ( verticalPanel.getWidget(verticalPanel.getWidgetIndex(leftSide)));
            System.out.println   (verticalPanel.getWidget(0).asWidget().getClass());
        }    */

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
                  file.getElement().setPropertyString("value", "");
                  addRow(filename);
              }
          });
      }

}