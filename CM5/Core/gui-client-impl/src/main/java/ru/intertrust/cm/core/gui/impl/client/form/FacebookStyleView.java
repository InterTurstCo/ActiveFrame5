package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserRowItem;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.      
 * User: Timofiy Bilyi
 * Date: 28.11.13
 * Time: 11:20
 * To change this template use File | Settings | File Templates.
 */
public class FacebookStyleView implements IsWidget{

    private AbsolutePanel mainBoxPanel;

    private ArrayList<TableBrowserRowItem> rowItems;

    public FacebookStyleView(){
        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box");

    }

    public ArrayList<TableBrowserRowItem> getRowItems() {
        return rowItems;
    }

    public void setRowItems(ArrayList<TableBrowserRowItem> rowItems) {
        this.rowItems = rowItems;
    }

    public void addRowItem(final TableBrowserRowItem rowItem){
        final AbsolutePanel element = new AbsolutePanel();
        element.setStyleName("facebook-element");
        Label label = new Label(rowItem.getSelectedRowRepresentation());
        label.setStyleName("facebook-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
               rowItems.remove(rowItem);
                element.removeFromParent();
            }
        });
        element.add(label);
        element.add(delBtn);
        mainBoxPanel.add(element);
    }

    public void showSelectedItems(){
        mainBoxPanel.clear();
        for(TableBrowserRowItem rowItem : rowItems){
            addRowItem(rowItem);
        }

    }

    @Override
    public Widget asWidget() {
        return mainBoxPanel;
    }
}


