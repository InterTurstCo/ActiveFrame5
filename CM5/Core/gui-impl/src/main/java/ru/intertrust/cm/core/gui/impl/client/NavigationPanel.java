package ru.intertrust.cm.core.gui.impl.client;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Компонент навигационной панели
 * @author Denis Mitavskiy
 *         Date: 17.07.13
 *         Time: 13:03
 */
public class NavigationPanel extends HorizontalLayout {
    private VerticalLayout leftPanel;
    private VerticalLayout rightPanel;

    public NavigationPanel() {
        leftPanel = new VerticalLayout();
        leftPanel.setSizeFull();
        leftPanel.setWidth("70px");
        leftPanel.addComponent(new Label("Left"));

        rightPanel = new VerticalLayout();
        rightPanel.setSizeFull();
        rightPanel.setWidth("130px");
        rightPanel.addComponent(new Label("Right"));

        addComponent(leftPanel);
        addComponent(rightPanel);
    }

    public VerticalLayout getLeftPanel() {
        return leftPanel;
    }

    public VerticalLayout getRightPanel() {
        return rightPanel;
    }
}
