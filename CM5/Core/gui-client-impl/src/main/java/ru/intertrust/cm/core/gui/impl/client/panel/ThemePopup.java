package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.config.ThemeConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.model.action.system.ThemeActionContext;

import java.util.Map;

import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.05.14
 *         Time: 10:25
 */
public class ThemePopup extends PopupPanel {
    private String userThemeComponentName;
    private Panel imageContainer;

    public ThemePopup(Map<String, ThemeConfig> themeMap) {
        initPopup(themeMap);
    }

    private void initPopup(Map<String, ThemeConfig> themeMap) {

        this.setStyleName("theme-popup");
        final AbsolutePanel container = new AbsolutePanel();
        AbsolutePanel themePreview = new AbsolutePanel();
        themePreview.setStyleName("preview-top");
        VerticalPanel radioButtonPanel = new VerticalPanel();
        radioButtonPanel.setStyleName("theme-list");
        imageContainer = new AbsolutePanel();
        imageContainer.setStyleName("preview-panel");

        String currentThemeComponentName = GlobalThemesManager.getCurrentThemeComponentName();
        userThemeComponentName = currentThemeComponentName;
        for (ThemeConfig themeConfig : themeMap.values()) {
            String themeName = themeConfig.getDisplayName();
            RadioButton radioButton = new RadioButton("Theme", themeName);
            radioButton.addClickHandler(new RadioButtonClickHandler(themeConfig));
            radioButtonPanel.add(radioButton);
            String imagePath = themeConfig.getImg();
            Image image = new Image(imagePath);
            imageContainer.add(image);
            String componentName = themeConfig.getComponentName();
            image.getElement().setId(componentName);
            if (currentThemeComponentName.equalsIgnoreCase(componentName)) {
                image.setVisible(true);
                radioButton.setValue(true, false);

            } else {
                image.setVisible(false);
            }
        }
        AbsolutePanel buttonPanel = new AbsolutePanel();
        buttonPanel.setStyleName("button-panel");
        buttonPanel.getElement().getStyle().clearPosition();
        Button select = new Button(LocalizeUtil.get(CHANGE_BUTTON));
        select.setStyleName("lightButton");
        select.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final ActionConfig actionConfig = new ActionConfig();
                actionConfig.setDirtySensitivity(false);
                actionConfig.setImmediate(true);
                final ThemeActionContext context = new ThemeActionContext();
                context.setActionConfig(actionConfig);
                context.setThemeName(userThemeComponentName);
                final Action action = ComponentRegistry.instance.get(ThemeActionContext.COMPONENT_NAME);
                action.setInitialContext(context);
                action.perform();
            }
        });
        buttonPanel.add(select);
        Button cancel = new Button(LocalizeUtil.get(CANCEL_BUTTON));
        cancel.setStyleName("darkButton");
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ThemePopup.this.hide();
            }
        });
        buttonPanel.add(cancel);
        themePreview.add(radioButtonPanel);
        themePreview.add(imageContainer);
        container.add(themePreview);
        container.add(buttonPanel);
        this.add(container);
        this.show();
    }

    private void changeImageVisibility(ThemeConfig themeConfig) {
        String imageId = themeConfig.getComponentName();
        for (Widget widget : imageContainer) {
            Element element = widget.getElement();
            if (element.getId().equalsIgnoreCase(imageId)) {
                widget.setVisible(true);
            } else {
                widget.setVisible(false);
            }
        }
    }


    private class RadioButtonClickHandler implements ClickHandler {
        private ThemeConfig themeConfig;

        private RadioButtonClickHandler(ThemeConfig themeConfig) {
            this.themeConfig = themeConfig;
        }

        @Override
        public void onClick(ClickEvent event) {
            userThemeComponentName = themeConfig.getComponentName();
            changeImageVisibility(themeConfig);
        }
    }

}
