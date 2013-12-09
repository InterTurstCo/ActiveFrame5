package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.form.widget.TableBrowserRowItem;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.12.13
 *         Time: 16:15
 */
public class FacebookStyleView implements IsWidget {
    private static final String DISPLAY_STYLE_INLINE = "inline";
    private static final String DISPLAY_STYLE_TABLE = "table";
    private AbsolutePanel mainBoxPanel;
    private Style.Display displayStyle;
    private ArrayList<TableBrowserRowItem> chosenItems;

    public FacebookStyleView() {

        mainBoxPanel = new AbsolutePanel();
        mainBoxPanel.setStyleName("facebook-main-box");
        chosenItems = new ArrayList<TableBrowserRowItem>();

    }

    public ArrayList<Id> getChosenIds() {
        ArrayList<Id> chosenIds = new ArrayList<Id>();
        for (TableBrowserRowItem rowItem : chosenItems) {
            chosenIds.add(rowItem.getId());
        }
        return chosenIds;
    }

    public void removeChosenItem(Id id) {

        for (TableBrowserRowItem chosenItem : chosenItems) {
            if (chosenItem.getId().equals(id)) {
                chosenItems.remove(chosenItem);
            }
        }
    }


    public ArrayList<TableBrowserRowItem> getChosenItems() {
        return chosenItems;
    }

    public void setChosenItems(ArrayList<TableBrowserRowItem> chosenItems) {
        this.chosenItems = chosenItems;
    }

    public void displayChosenRowItem(final TableBrowserRowItem rowItem) {
        final AbsolutePanel element = new AbsolutePanel();
        element.getElement().getStyle().setDisplay(displayStyle);
        element.setStyleName("facebook-element");
        Label label = new Label(rowItem.getSelectedRowRepresentation());
        label.setStyleName("facebook-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.getElement().getStyle().setPadding(2, Style.Unit.PX);
        delBtn.getElement().getStyle().setBackgroundColor("red");

        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                chosenItems.remove(rowItem);
                element.removeFromParent();
            }
        });
        element.add(label);
        element.add(delBtn);
        mainBoxPanel.add(element);
    }

    public void initDisplayStyle(String howToDisplay) {

        if (DISPLAY_STYLE_INLINE.equalsIgnoreCase(howToDisplay)) {
            displayStyle = Style.Display.INLINE_BLOCK;
        }
        if (DISPLAY_STYLE_TABLE.equalsIgnoreCase(howToDisplay)) {
            displayStyle = Style.Display.BLOCK;
        }

    }

    public void showSelectedItems() {
        mainBoxPanel.clear();
        for (TableBrowserRowItem rowItem : chosenItems) {
            displayChosenRowItem(rowItem);
        }

    }

    @Override
    public Widget asWidget() {
        return mainBoxPanel;
    }
}


