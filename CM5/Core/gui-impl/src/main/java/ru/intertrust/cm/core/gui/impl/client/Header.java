package ru.intertrust.cm.core.gui.impl.client;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

/**
 * @author Denis Mitavskiy
 *         Date: 15.07.13
 *         Time: 18:53
 */
public class Header extends HorizontalLayout {
    public Header() {
        super();
        addComponent(new Label("This is a Company Media Image"));
        addComponent(new TextField(""));
        addComponent(new Label("User Info"));
        addComponent(new Label("Settings"));
        addComponent(new Label("Help"));
        addComponent(new Label("Logout"));
        setHeight("70px");
        setWidth("100%");
    }

}
