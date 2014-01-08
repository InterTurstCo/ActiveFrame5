package ru.intertrust.cm.core.gui.impl.client.form;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.12.13
 *         Time: 13:15
 */
public class PopupButtons extends AbsolutePanel {
    private PopupPanel popup;

    public PopupButtons(PopupPanel popup) {
        this.popup = popup;
        createPanel();

    }

    private void createPanel() {
        Button okButton = new Button("OK");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popup.hide();
            }
        });
        Button cancelButton = new Button("CANCEL");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popup.hide();
            }
        });
        this.add(okButton);
        this.add(cancelButton);

    }
}
