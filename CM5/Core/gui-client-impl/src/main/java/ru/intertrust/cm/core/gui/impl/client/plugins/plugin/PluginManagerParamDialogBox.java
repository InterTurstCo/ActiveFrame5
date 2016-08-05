package ru.intertrust.cm.core.gui.impl.client.plugins.plugin;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.*;

/**
 * @author Andrei Lozovoy
 * @since 04.08.16
 */
public class PluginManagerParamDialogBox extends DialogBox{

    private String result;
    private Button okButton;
    private Button cancelButton;
    private TextBox parameterTextBox;

    public PluginManagerParamDialogBox() {
        init();
    }

    private void init(){
        // Enable animation.
        this.setAnimationEnabled(true);
        // Enable glass background.
        this.addStyleName("dialogBoxBody");
        this.addStyleName("body-popup-plugin-meneger");
        this.removeStyleName("gwt-DialogBox");
        // DialogBox is a SimplePanel, so you have to set its widget
        // property to whatever you want its contents to be.
        okButton= new Button("ОК");
        okButton.addStyleName("darkButton");
        okButton.removeStyleName("gwt-Button");

        AbsolutePanel panel = new AbsolutePanel();
        panel.addStyleName("dialog-box-content");

        panel.add(new Label("Параметр для плагина(палгинов)"));

        parameterTextBox = new TextBox();
        parameterTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if(Cookies.isCookieEnabled()){
                    Cookies.setCookie("PluginManagerParam", event.getValue());
                }
                result = event.getValue();
            }
        });

        if(Cookies.getCookie("PluginManagerParam") != null){
            parameterTextBox.setValue(Cookies.getCookie("PluginManagerParam"));
        }

        panel.add(parameterTextBox);

        AbsolutePanel buttonsPanel = new AbsolutePanel();
        buttonsPanel.addStyleName("attachments-buttons-panel");
        buttonsPanel.getElement().getStyle().clearPosition();
        buttonsPanel.add(okButton);
        cancelButton = new Button("Нет");
        cancelButton.addStyleName("lightButton");
        cancelButton.removeStyleName("gwt-Button");
        buttonsPanel.add(cancelButton);
        panel.add(buttonsPanel);
        this.add(panel);
    }

    public void showDialogBox() {
        this.center();
    }

    public void addOkButtonClickHandler(ClickHandler okClickHandler){
        okButton.addClickHandler(okClickHandler);
    }

    public void addCancelButtonClickHandler(ClickHandler cancelClickHandler){
        cancelButton.addClickHandler(cancelClickHandler);
    }

    public String getResult(){
        return result;
    }
}
