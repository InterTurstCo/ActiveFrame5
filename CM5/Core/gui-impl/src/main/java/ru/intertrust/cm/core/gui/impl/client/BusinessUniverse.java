package ru.intertrust.cm.core.gui.impl.client;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

/**
 * Главная страница приложения. "Бизнес-вселенная".
 *
 * @author Denis Mitavskiy
 *         Date: 05.07.13
 *         Time: 14:38
 */
public class BusinessUniverse extends UI {
    /**
     * Относительный путь к странице
     */
    public static final String PATH = "/business-universe";

    private Navigator navigator;
    protected static final String MAINVIEW = "main";

    @Override
    protected void init(VaadinRequest request) {
        getPage().setTitle("Business Universe");

        // Create a navigator to control the views
        navigator = new Navigator(this, this);

        // Create and register the views
        navigator.addView("", new BusinessUniverseMainView());
    }
}
