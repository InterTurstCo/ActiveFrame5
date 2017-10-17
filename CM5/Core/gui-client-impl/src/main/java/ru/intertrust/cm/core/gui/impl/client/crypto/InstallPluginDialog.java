package ru.intertrust.cm.core.gui.impl.client.crypto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class InstallPluginDialog extends DialogBox{
    private Label label;
    private VerticalPanel panel;

    public InstallPluginDialog() {
        // Set the dialog box's caption.
        setText("Не установлен CryptoPro Browser Plugin");

        // Enable animation.
        setAnimationEnabled(true);
        
        removeStyleName("gwt-DialogBox ");
        addStyleName("popup-body");


        Button close = new Button("Закрыть");
        close.removeStyleName("gwt-Button");
        close.addStyleName("lightButton");
        close.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                InstallPluginDialog.this.hide();
            }
        });
        
        label = new Label("Не установлен плагин CryptoPro Browse Plugin. Скачать и установить плагин можно по ссылке:");
        
        //Anchor link = new Anchor("Скачать плагин", GWT.getHostPageBaseURL() + "cryptopro/cadesplugin.exe");
        //Скачиваем прям с сайта
        Anchor link = new Anchor("Скачать плагин", "https://www.cryptopro.ru/products/cades/plugin/get_2_0");        
        Anchor instructions = new Anchor("Инструкция по установке", GWT.getHostPageBaseURL() + "cryptopro/instruction.html", "_blank");

        panel = new VerticalPanel();
        panel.setSpacing(10);
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        panel.setStyleName("grey-background");
        panel.add(label);
        panel.add(link);
        panel.add(instructions);        
        
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        buttonPanel.add(close);
        
        panel.add(buttonPanel);

        setWidget(panel);
        
        center();
    }    

}
