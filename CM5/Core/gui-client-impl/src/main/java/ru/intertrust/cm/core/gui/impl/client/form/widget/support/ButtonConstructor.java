package ru.intertrust.cm.core.gui.impl.client.form.widget.support;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 24.01.14
 * Time: 11:25
 * To change this template use File | Settings | File Templates.
 */
public class ButtonConstructor extends Composite{

    private AbsolutePanel buttonPanel = new AbsolutePanel();
    private FocusPanel focusPanel;
    private String image;
    private String text;

    public ButtonConstructor(FocusPanel focusPanel){
        this.focusPanel = focusPanel;
        init();
    }

    public ButtonConstructor(FocusPanel focusPanel, String image, String text) {
        this.focusPanel = focusPanel;
        this.image = image;
        this.text = text;
        init();
    }

    private void init(){
        initWidget(buttonPanel);
        createStyle();
        buildButton();
    }

    private void buildButton(){
        if (image != null && !image.equals("...") && image.length() !=0){
            addImage(image);
            if (text != null && !text.equals("...") && text.length() !=0){
                setTitleAtttibute(text);
            }

        } else if (text != null && !text.equals("...") && text.length() !=0){
           addText(text);
        }
    }

    private void createStyle(){
        focusPanel.setStyleName("hierarh-browser-inline composite-button");
        buttonPanel.setStyleName("hierarh-browser-button");
        buttonPanel.getElement().getStyle().setHeight(20, Style.Unit.PX);



    }


    public void addImage(String img){
        Image pic = new Image(img);
        pic.setStyleName("hierarh-browser-inline-margin-left");
        buttonPanel.insert(pic, 0);
    }

    public void addText(String lbl){
        Label label = new Label(lbl);
        label.setStyleName("hierarh-browser-inline-margin-left");
        buttonPanel.add(label);
    }

    public void clearButton(){
        buttonPanel.clear();
    }

    public void setTitleAtttibute(String value){
        buttonPanel.getElement().setAttribute("title", value);
    }

}
