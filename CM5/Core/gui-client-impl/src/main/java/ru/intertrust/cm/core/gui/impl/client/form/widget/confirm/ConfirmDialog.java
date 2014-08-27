package ru.intertrust.cm.core.gui.impl.client.form.widget.confirm;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.gui.api.client.ConfirmCallback;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 09.08.2014
 *         Time: 15:50
 */
public class ConfirmDialog extends DialogBox {
    private ConfirmCallback confirmCallback;
    private String text;

    public ConfirmDialog(String text, ConfirmCallback confirmCallback) {
        this.text = text;
        this.confirmCallback = confirmCallback;
        init(text);
    }

    private void init(String text) {
        this.setAnimationEnabled(true);
        this.setStyleName("confirmDialogWindow");

        Label label = new Label(text);
        label.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().confirmDialogIm());
        AbsolutePanel panel = new AbsolutePanel();
        panel.setStyleName("confirmDialogContent");

        panel.add(label);
        Panel buttonsPanel = initButtonsPanel();
        panel.add(buttonsPanel);
        this.add(panel);
    }

    private Panel initButtonsPanel() {
        AbsolutePanel buttonsPanel = new AbsolutePanel();
        buttonsPanel.setStyleName("buttonsPanel");
        Button okButton = new Button("ОК");
        okButton.setStyleName("dark-button");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doAffirmative();
            }
        });
        buttonsPanel.add(okButton);
        Button cancelButton = new Button("Отмена");
        cancelButton.setStyleName("light-button");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doCancel();
            }
        });
        buttonsPanel.add(cancelButton);
        return buttonsPanel;
    }

    private void doAffirmative() {
        this.hide();
        if (confirmCallback != null) {
            confirmCallback.onAffirmative();
        }
    }

    private void doCancel() {
        this.hide();
        if (confirmCallback != null) {
            confirmCallback.onCancel();
        }
    }
    public void confirm(){
        this.show();
        Style style = this.getElement().getStyle();
        style.setTop(80, Style.Unit.PX);
        style.setLeft(39, Style.Unit.PCT);


    }

}
