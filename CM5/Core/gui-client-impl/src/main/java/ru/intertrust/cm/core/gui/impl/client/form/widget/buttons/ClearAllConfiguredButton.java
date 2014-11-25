package ru.intertrust.cm.core.gui.impl.client.form.widget.buttons;


import ru.intertrust.cm.core.config.gui.form.widget.buttons.ButtonConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.11.2014
 *         Time: 10:57
 */
public class ClearAllConfiguredButton extends ConfiguredButton {
    public ClearAllConfiguredButton(ButtonConfig buttonConfig) {
        super(buttonConfig);
    }

    @Override
    protected void buildDefault() {
        throw new IllegalStateException("There no config for the button");
    }
}
