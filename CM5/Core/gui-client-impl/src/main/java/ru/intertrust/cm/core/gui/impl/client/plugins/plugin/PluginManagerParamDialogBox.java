package ru.intertrust.cm.core.gui.impl.client.plugins.plugin;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.plugin.PluginInfo;
import ru.intertrust.cm.core.gui.impl.client.form.widget.messagedialog.DialogBoxUtils;

import java.util.List;

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
        this.setAnimationEnabled(true);

        this.addStyleName("dialogBoxBody");
        this.addStyleName("body-popup-plugin-meneger");
        this.removeStyleName("gwt-DialogBox");

        okButton= new Button("Выполнить");
        okButton.addStyleName("darkButton");
        okButton.removeStyleName("gwt-Button");

        AbsolutePanel panel = new AbsolutePanel();
        panel.addStyleName("dialog-box-content");

        panel.add(new Label("Параметр для плагина (плагинов)"));

        parameterTextBox = new TextBox();
        parameterTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                result = event.getValue();
            }
        });

        panel.add(parameterTextBox);

        AbsolutePanel buttonsPanel = new AbsolutePanel();
        buttonsPanel.addStyleName("attachments-buttons-panel");
        buttonsPanel.getElement().getStyle().clearPosition();
        buttonsPanel.add(okButton);
        cancelButton = new Button("Отмена");
        cancelButton.addStyleName("lightButton");
        cancelButton.removeStyleName("gwt-Button");
        buttonsPanel.add(cancelButton);
        panel.add(buttonsPanel);

        DialogBoxUtils.addCaptionCloseButton(this);
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

    public void checkAndSetParamValue(List<PluginInfo> pluginInfos){
        String param = null;
        boolean matched = false;
        for(PluginInfo pluginInfo: pluginInfos){
            if(pluginInfo.isChecked()) {
                String cookieForPlugin = Cookies.getCookie(pluginInfo.getClassName());
                if(param == null && cookieForPlugin != null) {
                    param = cookieForPlugin;
                    matched = true;
                }else if(param != null && !param.equals(cookieForPlugin)){
                    matched = false;
                    break;
                }
            }
        }

        if(matched){
            parameterTextBox.setValue(param);
            result = param;
        }else {
            parameterTextBox.setValue("");
            result = "";
        }
    }
}
