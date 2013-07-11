package ru.intertrust.cm.core.gui.impl.client;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Denis Mitavskiy
 *         Date: 10.07.13
 *         Time: 16:41
 */
public class BusinessUniverseMainView extends VerticalLayout implements View {
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        addComponent(new Label("Good day"));
    }
}
