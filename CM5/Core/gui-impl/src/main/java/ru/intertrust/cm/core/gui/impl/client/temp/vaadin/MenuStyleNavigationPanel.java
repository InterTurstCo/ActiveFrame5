package ru.intertrust.cm.core.gui.impl.client.temp.vaadin;

import com.vaadin.ui.MenuBar;

/**
 * @author Denis Mitavskiy
 *         Date: 17.07.13
 *         Time: 13:19
 */
public class MenuStyleNavigationPanel extends MenuBar {
    public MenuStyleNavigationPanel() {
        MenuItem action = addItem("Действие", null, null);
        action.addItem("Полный экран", null, null);
    }
}
