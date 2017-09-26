package ru.intertrust.cm.core.gui.impl.client.form.widget.buttons;

import ru.intertrust.cm.core.config.gui.form.widget.buttons.ButtonConfig;

/**
 * Created by Ravil on 26.09.2017.
 */
public class DefaultConfiguredButton extends ConfiguredButton {

    public DefaultConfiguredButton(ButtonConfig buttonConfig) {
        super(buttonConfig);
    }

    @Override
    protected void buildDefault() {
        throw new IllegalStateException("There no config for the button");
    }
}
