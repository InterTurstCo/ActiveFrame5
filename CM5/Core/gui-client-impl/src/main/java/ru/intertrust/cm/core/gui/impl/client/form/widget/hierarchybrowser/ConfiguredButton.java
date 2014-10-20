package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.form.widget.buttons.ButtonConfig;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.10.2014
 *         Time: 10:56
 */
public class ConfiguredButton extends Composite implements HasClickHandlers {
    private Panel root;
    public ConfiguredButton(ButtonConfig buttonConfig) {
        root = new AbsolutePanel();
        build(buttonConfig);
        initWidget(root);
    }

    private void build(ButtonConfig buttonConfig) {
        if (buttonConfig == null) {
            buildDefault();
        } else {
            buildFromConfig(buttonConfig);
        }

    }

    private void buildDefault() {
        Panel image = new AbsolutePanel();
        image.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().addDoBtn());
        root.add(image);
    }

    private void buildFromConfig(ButtonConfig buttonConfig) {
        root.addStyleName("light-button button-extra-style");
        String imagePath = buttonConfig.getImage();
        if (imagePath != null) {
            Image image = new Image(GlobalThemesManager.getResourceFolder() + imagePath);
            root.add(image);
        }
        String text = buttonConfig.getText();
        if (text != null) {
            Label label = new Label(text);
            label.addStyleName("hierarchyBrowserLabel");
            root.add(label);

        }

    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return root.addDomHandler(handler, ClickEvent.getType());
    }
}
