package ru.intertrust.cm.core.gui.impl.client.form.widget.buttons;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;
import ru.intertrust.cm.core.config.gui.form.widget.buttons.ButtonConfig;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.10.2014
 *         Time: 21:04
 */
public class LinkCreatingButton extends ConfiguredButton {
    public LinkCreatingButton(ButtonConfig buttonConfig) {
        super(buttonConfig);
    }

    @Override
    protected void buildDefault() {
        Panel image = new AbsolutePanel();
        image.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().addDoBtn());
        root.add(image);
    }

}
