package ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;


/**
 * @author Yaroslav Bondacrhuk
 *         Date: 07.08.2014
 *         Time: 22:14
 */
public abstract class MessageDialog extends DialogBox {

    protected abstract String getContentStyleName();

    protected abstract String getDialogStyleName();

    protected abstract String getMessageStyleName();

    public MessageDialog(String text) {

        init(text);
    }

    private void init(String text) {
        this.setAnimationEnabled(true);
        this.setStyleName(getDialogStyleName());

        Button okButton = new Button("ОК");
        okButton.setStyleName("darkButton");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MessageDialog.this.hide();
            }
        });

        Label label = new Label(text);
        label.setStyleName(getMessageStyleName());
        AbsolutePanel panel = new AbsolutePanel();
        panel.setStyleName(getContentStyleName());

        panel.add(label);
        AbsolutePanel buttonsPanel = new AbsolutePanel();
        buttonsPanel.setStyleName("buttonsPanel");

        buttonsPanel.add(okButton);

        panel.add(buttonsPanel);
        this.add(panel);
    }

    public void alert(){
        this.show();
        Style style = this.getElement().getStyle();
        style.setTop(80, Style.Unit.PX);
        style.setLeft(39, Style.Unit.PCT);
    }


}
