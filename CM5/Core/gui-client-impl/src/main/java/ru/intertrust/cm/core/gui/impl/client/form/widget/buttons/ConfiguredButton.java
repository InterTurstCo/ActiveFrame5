package ru.intertrust.cm.core.gui.impl.client.form.widget.buttons;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.form.widget.buttons.ButtonConfig;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.10.2014
 *         Time: 10:56
 */
public abstract class ConfiguredButton extends Composite implements HasClickHandlers {
    protected Panel root;
    private ButtonConfig bConfig;

    public ConfiguredButton(ButtonConfig buttonConfig) {
        bConfig = buttonConfig;
        root = new AbsolutePanel();
        build();
        initWidget(root);
    }

    private void build() {
        if (bConfig == null) {
            buildDefault();
        } else {
            buildFromConfig();
        }

    }

    protected abstract void buildDefault();

    protected void buildFromConfig() {
        root.addStyleName(getContainerStyle());
        String imagePath = bConfig.getImage();
        buildImageFromConfig(imagePath);
        String text = bConfig.getText();
        buildTextFromConfig(text);
        root.setVisible(bConfig.isDisplay());
    }

    protected void buildImageFromConfig(String imagePath) {
        if (imagePath != null) {
            Image image = new Image(GlobalThemesManager.getResourceFolder() + imagePath);
            image.setStyleName(getImageStyle());
            root.add(image);
        }
    }

    protected void buildTextFromConfig(String text) {
        if (text != null) {
            Label label = new Label(text);
            label.setStyleName(getTitleStyle());
            root.add(label);

        }
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return root.addDomHandler(handler, ClickEvent.getType());
    }

    protected String getTitleStyle() {
        if (bConfig != null && bConfig.getTextStyleName() != null)
            return bConfig.getTextStyleName();
        else
            return "linkEditingButtonText";
    }


    protected String getImageStyle() {
        return BusinessUniverseConstants.EMPTY_VALUE;
    }


    protected String getContainerStyle() {
        if (bConfig != null && bConfig.getContainerStyleName() != null)
            return bConfig.getContainerStyleName();
        else
            return "darkButton";
    }

}
