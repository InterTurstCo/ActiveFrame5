package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.user.client.ui.*;

import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: Timofiy Bilyi
 * Date: 27.11.13
 * Time: 22:29
 * To change this template use File | Settings | File Templates.
 */
public class SuggestBoxHelper implements IsWidget{
    private AbsolutePanel suggestDecoration;
    private AbsolutePanel suggestMainBox;
    private AbsolutePanel activeTextField;

    public SuggestBoxHelper(){
        suggestMainBox = new AbsolutePanel();
        suggestMainBox.setStyleName("suggest-main-box");
        activeTextField = new AbsolutePanel();
        activeTextField.setStyleName("active-text-field");
        AbsolutePanel suggestList = new AbsolutePanel();
        suggestList.setStyleName("suggest-list");
        suggestDecoration = new AbsolutePanel();
        suggestDecoration.setStyleName("suggest-decoration");

        suggestMainBox.add(activeTextField);
        suggestMainBox.add(suggestList);
        suggestList.add(suggestDecoration);

    }

    public void addAttachmentElement(String title){
        AbsolutePanel element = new AbsolutePanel();
        element.setStyleName("suggest-element");
        Label label = new Label(title);
        label.setStyleName("suggest-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("suggest-btn");
        element.add(label);
        element.add(delBtn);
        activeTextField.add(element);
    }

    public void showSuggests(List<String> suggestList){
        for( String suggest : suggestList ){
            AbsolutePanel suggestElementList = new AbsolutePanel();
            suggestElementList.setStyleName("suggest-element-list");
            suggestDecoration.add(new Label(suggest));
        }

    }

    @Override
    public Widget asWidget() {
        return suggestMainBox;
    }
}
