package ru.intertrust.cm.core.gui.impl.client.crypto;

import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.SimpleTextTooltip;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ProgressDialog extends DialogBox {
    private boolean cancel;
    private Label label;
    private VerticalPanel panel;
    private int height;
    private int width;
    private Image progressBar;
    private Button ok;

    public ProgressDialog() {
        // Set the dialog box's caption.
        setText("Выполняется подпись");

        // Enable animation.
        setAnimationEnabled(true);

        // Enable glass background.
        //setGlassEnabled(true);
        
        removeStyleName("gwt-DialogBox ");
        addStyleName("popup-body");

        // DialogBox is a SimplePanel, so you have to set its widget 
        // property to whatever you want its contents to be.
        ok = new Button("Прервать");
        ok.removeStyleName("gwt-Button");
        ok.addStyleName("lightButton");
        ok.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                cancel = true;
                ProgressDialog.this.hide();
            }
        });

        label = new Label("Message");
        
        progressBar = new Image();
        progressBar.setUrl("CMJSpinner.gif");
        
        height = 250;
        width = 400;

        panel = new VerticalPanel();
        panel.setHeight(String.valueOf(height));
        panel.setWidth(String.valueOf(width));
        panel.setSpacing(10);
        panel.setStyleName("grey-background");
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        //panel.setStyleName("infoDialogContent");
        panel.add(label);
        panel.add(progressBar);

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        buttonPanel.add(ok);
        
        panel.add(buttonPanel);        
        
        setWidget(panel);
        
        center();
    }
    
    public void setMaxValue(int maxValue){
        
    }
    
    public boolean isCancel(){
        return cancel;
    }
    
    public void setValue(int maxValue){
        
    }

    public void setMessage(String message){
        label.setText(message);
    }
    
    public int getHeight(){
        return height;
    }

    public int getWidth(){
        return width;
    }

    public void showSuccess() {
        label.setText("Подпись завершена успешно");
        ok.setText("Закрыть");
        
        panel.remove(progressBar);
        final Timer closeTimer = new Timer() {
            @Override
            public void run() {
                ProgressDialog.this.hide();
            }
        };
        closeTimer.schedule(2000);        
    }    
}
