package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

/**
 * Created with IntelliJ IDEA.
 * User: tbilyi
 * Date: 13.11.13
 * Time: 18:51
 * To change this template use File | Settings | File Templates.
 */
public class InformationDialogBox implements IsWidget{
    private DialogBox dialogBox;
    private VerticalPanel verticalPanel;

    public InformationDialogBox(String name, String lastName, String login, String email){
        dialogBox = new DialogBox();
        dialogBox.addStyleName("infoDialogBox");
        verticalPanel = new VerticalPanel();
        addUserInfo(name, lastName, login, email);
        addButton();
        dialogBox.add(verticalPanel);
    }


    @Override
    public Widget asWidget() {
        return verticalPanel;
    }

    private void addUserInfo(String name, String lastName, String login, String email){
        verticalPanel.add(new Label(LocalizeUtil.get(LOGIN) + "  " + login));
        verticalPanel.add(new Label(LocalizeUtil.get(FIRST_NAME) + "  " + name));
        verticalPanel.add(new Label(LocalizeUtil.get(LAST_NAME) + "  " + lastName));
        verticalPanel.add(new Label(LocalizeUtil.get(EMAIL) + "  " + email));
    }

    private void addButton(){
        Button closeButton = new Button(LocalizeUtil.get(CLOSE_BUTTON));
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        verticalPanel.add(closeButton);
    }

    public void show(){
        dialogBox.setPopupPosition(Window.getClientWidth()/2,Window.getClientHeight()/2);
        dialogBox.show();
    }
}
