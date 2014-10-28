package ru.intertrust.cm.core.gui.impl.client.form.widget.buttons;

import ru.intertrust.cm.core.config.gui.form.widget.buttons.ButtonConfig;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.10.2014
 *         Time: 20:58
 */
public class HierarchyConfiguredButton extends ConfiguredButton {

    public HierarchyConfiguredButton(ButtonConfig buttonConfig) {
        super(buttonConfig);
    }

    @Override
    protected String getTitleStyle() {
        return "gwt-Label hierarchyBrowserLabel";
    }

    @Override
    protected String getImageStyle() {
        return BusinessUniverseConstants.EMPTY_VALUE;
    }

    @Override
    protected String getContainerStyle() {
        return "light-button button-extra-style";
    }
}
