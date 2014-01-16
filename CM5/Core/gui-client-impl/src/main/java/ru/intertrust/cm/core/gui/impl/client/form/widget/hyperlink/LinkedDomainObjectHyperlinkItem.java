package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public class LinkedDomainObjectHyperlinkItem implements IsWidget{

    private Label label;

    public AbsolutePanel initWidget() {
        final AbsolutePanel element = new AbsolutePanel();
        element.getElement().getStyle().setMarginLeft(5, Style.Unit.PX);

        element.setStyleName("facebook-element");
        label = new Label();
        label.setStyleName("facebook-label");
        FocusPanel delBtn = new FocusPanel();
        delBtn.addStyleName("facebook-btn");
        delBtn.getElement().getStyle().setPadding(2, Style.Unit.PX);
        delBtn.getElement().getStyle().setBackgroundColor("red");

        delBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                element.removeFromParent();
            }
        });
        element.add(label);
        element.add(delBtn);
        return element;
    }
    public void addItemClickHandler(ClickHandler clickHandler){
        label.addClickHandler(clickHandler);
    }
    public void setText(String text){
        label.setText(text);
    }

    @Override
    public Widget asWidget() {
        return initWidget();
    }
}
