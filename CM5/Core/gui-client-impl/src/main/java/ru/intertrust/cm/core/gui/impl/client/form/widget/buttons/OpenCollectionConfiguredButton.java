package ru.intertrust.cm.core.gui.impl.client.form.widget.buttons;

import com.google.gwt.user.client.ui.Label;
import ru.intertrust.cm.core.config.gui.form.widget.buttons.ButtonConfig;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.11.2014
 *         Time: 22:55
 */
public class OpenCollectionConfiguredButton extends ConfiguredButton {
    public OpenCollectionConfiguredButton(ButtonConfig buttonConfig) {
        super(buttonConfig);
    }

    @Override
    protected void buildDefault() {
        root.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().arrowDownButton());
    }

    @Override
    protected void buildImageFromConfig(String imagePath) {
        if (imagePath != null && !imagePath.equalsIgnoreCase("...")) {
            super.buildImageFromConfig(imagePath);
        }
    }

    @Override
    protected void buildTextFromConfig(String text) {
        if (text != null && text.equalsIgnoreCase("...")) {
            Label label = new Label("Добавить");
            label.setStyleName(getTitleStyle());
            root.add(label);
        } else {
            super.buildTextFromConfig(text);
        }
    }
}
