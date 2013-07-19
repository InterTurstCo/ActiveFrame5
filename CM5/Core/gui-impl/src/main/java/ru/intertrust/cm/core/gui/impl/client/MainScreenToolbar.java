package ru.intertrust.cm.core.gui.impl.client;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * @author Denis Mitavskiy
 *         Date: 17.07.13
 *         Time: 14:56
 */
public class MainScreenToolbar extends HorizontalLayout {
    private Label actionsButton;
    private Button navigationPanelSizeButton;
    private MenuStyleNavigationPanel navigationMenu;
    private Button horizontalSplitButton;
    private Button verticalSplitButton;
    private Button utilityPanelSizeButton;

    public MainScreenToolbar() {
        actionsButton = new Label("Actions");
        navigationPanelSizeButton = new Button("Toggle");
        navigationMenu = new MenuStyleNavigationPanel();
        horizontalSplitButton = new Button("HSplit");
        verticalSplitButton = new Button("VSplit");
        utilityPanelSizeButton = new Button("Utility");

        addComponent(actionsButton);
        addComponent(navigationPanelSizeButton);
        addComponent(navigationMenu);
        addComponent(horizontalSplitButton);
        addComponent(verticalSplitButton);
        addComponent(utilityPanelSizeButton);

        setWidth("100%");
    }
}
