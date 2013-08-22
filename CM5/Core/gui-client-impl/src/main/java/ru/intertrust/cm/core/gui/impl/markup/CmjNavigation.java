package ru.intertrust.cm.core.gui.impl.markup;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CmjNavigation extends VerticalPanel {

    public CmjNavigation() {
        super();
        drawButtons();
    }

    void elementBorder(Image element) {
        element.getElement().getStyle().setProperty("marginLeft", "5px");// ?
        element.getElement().getStyle().setProperty("borderStyle", "solid");
        element.getElement().getStyle().setProperty("borderWidth", "1");
        element.getElement().getStyle().setProperty("borderColor", "BLACK");
    }

    void drawButtons() {
        Image btnNavigation1 = new Image("images/inbox.png");
        Image btnNavigation2 = new Image("images/tasks.png");
        Image btnNavigation3 = new Image("images/calendar.png");
        Image btnNavigation4 = new Image("images/docs.png");
        Image btnNavigation5 = new Image("images/cases.png");
        Image btnNavigation6 = new Image("images/helpers.png");
        Image btnNavigation7 = new Image("images/analitika.png");

        elementBorder(btnNavigation1);
        elementBorder(btnNavigation2);
        elementBorder(btnNavigation3);
        elementBorder(btnNavigation4);
        elementBorder(btnNavigation5);
        elementBorder(btnNavigation6);
        elementBorder(btnNavigation7);

        this.add(btnNavigation1);
        this.add(btnNavigation2);
        this.add(btnNavigation3);
        this.add(btnNavigation4);
        this.add(btnNavigation5);
        this.add(btnNavigation6);
        this.add(btnNavigation7);
    }
}
