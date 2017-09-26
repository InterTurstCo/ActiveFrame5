package ru.intertrust.cm.core.gui.impl.client.form.widget.buttons;

import com.google.gwt.user.client.ui.Label;
import ru.intertrust.cm.core.config.gui.form.widget.buttons.ButtonConfig;

/**
 * Created by Ravil on 26.09.2017.
 */
public class SelectConfiguredButton extends OpenCollectionConfiguredButton {

    public SelectConfiguredButton(ButtonConfig buttonConfig) {
        super(buttonConfig);
    }
    @Override
    protected void buildTextFromConfig(String text) {
        if (text != null && text.equalsIgnoreCase("...")) {
            Label label = new Label("Выбрать");
            label.setStyleName(getTitleStyle());
            root.add(label);
        } else {
            super.buildTextFromConfig(text);
        }
    }
}
