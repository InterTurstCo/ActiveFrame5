package ru.intertrust.cm.core.gui.impl.client.crypto;

import java.util.ArrayList;

import ru.intertrust.cm.core.gui.model.form.HorizontalAlignment;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SelectCertificateDialog extends DialogBox {
    private Label label;
    private VerticalPanel panel;
    private int height;
    private int width;
    private int result=-1;

    public SelectCertificateDialog(ArrayList<String> certInfos) {
        // Set the dialog box's caption.
        setText("Выбор сертификата из доступных");

        // Enable animation.
        setAnimationEnabled(true);
        
        removeStyleName("gwt-DialogBox ");
        addStyleName("popup-body");
        final ListBox certificateList = new ListBox();

        // DialogBox is a SimplePanel, so you have to set its widget 
        // property to whatever you want its contents to be.
        Button ok = new Button("Выбрать");
        ok.removeStyleName("gwt-Button");
        ok.addStyleName("lightButton");
        ok.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                result = Integer.parseInt(certificateList.getSelectedValue());
                SelectCertificateDialog.this.hide();
            }
        });

        Button cancel = new Button("Закрыть");
        cancel.removeStyleName("gwt-Button");
        cancel.addStyleName("lightButton");
        cancel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                SelectCertificateDialog.this.hide();
            }
        });
        
        label = new Label("Выберите сертификат");
        
        height = 250;
        width = 400;
        
        for (int i=0; i<certInfos.size(); i++) {
            certificateList.addItem(certInfos.get(i), String.valueOf(i));
        }
        
        panel = new VerticalPanel();
        panel.setHeight(String.valueOf(height));
        panel.setWidth(String.valueOf(width));
        panel.setSpacing(10);
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        panel.setStyleName("grey-background");
        panel.add(label);
        panel.add(certificateList);
        
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        
        panel.add(buttonPanel);

        setWidget(panel);
        
        center();
    }
    
    public int getResult(){
        return result;
    }    
    
}
