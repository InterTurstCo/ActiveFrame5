package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.user.client.ui.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.      
 * User: Timofiy Bilyi
 * Date: 28.11.13
 * Time: 11:20
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentHelper implements IsWidget{
    
    private AbsolutePanel attachmentDecoration;
    private AbsolutePanel attachmentMainBox;
    private AbsolutePanel attachmentActiveTextField;

    public AttachmentHelper(){
        attachmentMainBox = new AbsolutePanel();
        attachmentMainBox.setStyleName("attachment-main-box");
        attachmentActiveTextField = new AbsolutePanel();
        attachmentActiveTextField.setStyleName("active-text-field");
        AbsolutePanel attachmentList = new AbsolutePanel();
        attachmentList.setStyleName("attachment-list");
        attachmentDecoration = new AbsolutePanel();
        attachmentDecoration.setStyleName("attachment-decoration");

        attachmentMainBox.add(attachmentActiveTextField);
        attachmentMainBox.add(attachmentList);
        attachmentList.add(attachmentDecoration);

    }

    public void addAttachmentElement(String title){
        AbsolutePanel element = new AbsolutePanel();
        element.setStyleName("attachment-element");
        Label label = new Label(title);
        label.setStyleName("attachment-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("attachment-btn");
        element.add(label);
        element.add(delBtn);
        attachmentActiveTextField.add(element);
    }

    public void showAttachments(List<String> attachmentList){
        for( String attachment : attachmentList){
            AbsolutePanel attachmentElementList = new AbsolutePanel();
            attachmentElementList.setStyleName("attachment-element-list");
            attachmentDecoration.add(new Label(attachment));
        }

    }

    @Override
    public Widget asWidget() {
        return attachmentMainBox;
    }
}


