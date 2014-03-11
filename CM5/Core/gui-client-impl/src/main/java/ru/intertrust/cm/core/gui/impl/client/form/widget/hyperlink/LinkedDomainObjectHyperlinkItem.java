package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public class LinkedDomainObjectHyperlinkItem extends Composite {

    private Label label;

    public LinkedDomainObjectHyperlinkItem(boolean editable) {
     if (editable)  {
         createWidgetEditable();
     }  else {
         createWidgetNoneEditable();
     }
    }

    public void createWidgetEditable() {
        final AbsolutePanel element = new AbsolutePanel();
        element.setStyleName("facebook-element");
        label = new Label();
        label.setStyleName("facebook-clickable-label");
        label.addStyleName("facebook-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                element.removeFromParent();
            }
        });
        element.add(label);
        element.add(delBtn);
        initWidget(element);

    }

    public void addItemClickHandler(ClickHandler clickHandler) {
        label.addClickHandler(clickHandler);
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void createWidgetNoneEditable() {
        final AbsolutePanel element = new AbsolutePanel();
        element.setStyleName("facebook-element");
        label = new Label();
        label.setStyleName("facebook-clickable-label");
        label.addStyleName("facebook-label");
        element.add(label);
        initWidget(element);

    }

}
