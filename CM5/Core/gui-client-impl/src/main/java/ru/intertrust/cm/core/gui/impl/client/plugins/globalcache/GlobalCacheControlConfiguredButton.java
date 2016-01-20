package ru.intertrust.cm.core.gui.impl.client.plugins.globalcache;

import ru.intertrust.cm.core.config.gui.form.widget.buttons.ButtonConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
public class GlobalCacheControlConfiguredButton extends ConfiguredButton {
    public GlobalCacheControlConfiguredButton(ButtonConfig buttonConfig) {
        super(buttonConfig);
    }

    @Override
    protected void buildDefault() {

    }

    protected String getTitleStyle() {
        return GlobalCacheControlUtils.STYLE_TOP_MENU_BUTTON_TEXT;
    }

    protected String getContainerStyle() {
        return GlobalCacheControlUtils.STYLE_TOP_MENU_BUTTON;
    }
}
