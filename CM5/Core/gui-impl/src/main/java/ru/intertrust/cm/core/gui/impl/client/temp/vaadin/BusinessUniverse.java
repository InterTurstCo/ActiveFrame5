package ru.intertrust.cm.core.gui.impl.client.temp.vaadin;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;

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
    private Header header;
    private NavigationPanel navigationPanel;
    private MainScreenToolbar mainScreenToolbar;

    @Override
    protected void init(VaadinRequest request) {
        getPage().setTitle("Business Universe");

        /*// Create a navigator to control the views
        navigator = new Navigator(this, this);

        // Create and register the views
        navigator.addView("", new BusinessUniverseMainView());*/
        setContent(renderMainLayout());
    }

    public VerticalLayout renderMainLayout() {

        header = new Header();
        navigationPanel = new NavigationPanel();
        mainScreenToolbar = new MainScreenToolbar();

        VerticalSplitPanel rightVerticalSplitPanel = new VerticalSplitPanel();

        Panel applicationFrame = new Panel();
        applicationFrame.setContent(rightVerticalSplitPanel);
        applicationFrame.setSizeFull();

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSizeFull();
        horizontalLayout.addComponent(navigationPanel);
        horizontalLayout.addComponent(applicationFrame);
        horizontalLayout.setExpandRatio(applicationFrame, 5);

        // add header
        VerticalLayout rootVerticalLayout = new VerticalLayout();
        rootVerticalLayout.setSizeFull();
        rootVerticalLayout.addComponent(header);
        rootVerticalLayout.addComponent(mainScreenToolbar);
        // navigation + content
        rootVerticalLayout.addComponent(horizontalLayout);
        rootVerticalLayout.setExpandRatio(horizontalLayout, 1);

        // vertical split panel
        return rootVerticalLayout;
    }
}
