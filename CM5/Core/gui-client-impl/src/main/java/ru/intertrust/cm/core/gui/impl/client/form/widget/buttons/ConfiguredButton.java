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

    protected abstract void buildDefault() ;

    protected void buildFromConfig(ButtonConfig buttonConfig) {
        root.addStyleName(getContainerStyle());
        String imagePath = buttonConfig.getImage();
        buildImageFromConfig(imagePath);
        String text = buttonConfig.getText();
        buildTextFromConfig(text);

    }
    protected void buildImageFromConfig(String imagePath){
        if (imagePath != null) {
            Image image = new Image(GlobalThemesManager.getResourceFolder() + imagePath);
            image.setStyleName(getImageStyle());
            root.add(image);
        }
    }
    protected void buildTextFromConfig(String text){
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
        return "linkEditingButtonText";
    }


    protected String getImageStyle() {
        return BusinessUniverseConstants.EMPTY_VALUE;
    }


    protected String getContainerStyle() {
        return "darkButton";
    }

}
