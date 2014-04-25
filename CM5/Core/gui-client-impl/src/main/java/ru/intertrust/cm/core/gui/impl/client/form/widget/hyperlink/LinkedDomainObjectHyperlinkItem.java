package ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink;

import com.google.gwt.dom.client.Style;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

import com.google.gwt.user.client.ui.Label;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.01.14
 *         Time: 10:25
 */
public class LinkedDomainObjectHyperlinkItem extends Composite {
    private AbsolutePanel element;
    private Label label;

    public LinkedDomainObjectHyperlinkItem() {
        createWidget();
    }

    public void addItemClickHandler(ClickHandler clickHandler) {
        label.addClickHandler(clickHandler);
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void createWidget() {
        element = new AbsolutePanel();
        element.setStyleName("facebook-element");
        label = new Label();
        label.setStyleName("facebook-clickable-label");
        label.addStyleName("facebook-label");
        element.add(label);
        initWidget(element);
    }

    public void hideWidget() {
        element.getElement().getStyle().setDisplay(Style.Display.NONE);
    }

    public void showWidget() {
        element.getElement().getStyle().clearDisplay();
    }
}
