package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.config.LanguageConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.model.action.system.LanguageActionContext;

import java.util.Map;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CANCEL_BUTTON_KEY;
import static ru.intertrust.cm.core.config.localization.LocalizationKeys.CHANGE_BUTTON_KEY;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CANCEL_BUTTON;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.CHANGE_BUTTON;

/**
 * @author Lesia Puhova
 *         Date: 03.03.2015
 *         Time: 16:44
 */
public class LanguagePopup extends PopupPanel {
    private String selectedLocale = Application.getInstance().getCurrentLocale();

    public LanguagePopup(Map<String, LanguageConfig> langMap) {
        initPopup(langMap);
    }

    private void initPopup(Map<String, LanguageConfig> langMap) {
        this.setStyleName("theme-popup");
        final AbsolutePanel container = new AbsolutePanel();
        AbsolutePanel langPanel = new AbsolutePanel();
        langPanel.setStyleName("preview-top");
        VerticalPanel radioButtonPanel = new VerticalPanel();
        radioButtonPanel.setStyleName("theme-list");
        for (LanguageConfig langConfig : langMap.values()) {
            String imagePath = langConfig.getImg();
            String displayName = langConfig.getDisplayName();
            String html = displayName;
            if (imagePath != null) {
                Image image = new Image(imagePath);
                image.getElement().getStyle().setFloat(Style.Float.RIGHT);
                image.getElement().getStyle().setPaddingLeft(20, Style.Unit.PX);
                html = html + image.getElement().getString();
            }
            RadioButton radioButton = new RadioButton("Lang", html, true);
            radioButton.addClickHandler(new RadioButtonClickHandler(langConfig));
            radioButtonPanel.add(radioButton);
            if (selectedLocale.equals(langConfig.getName())) {
                radioButton.setValue(true, false);
            }
        }
        AbsolutePanel buttonPanel = new AbsolutePanel();
        buttonPanel.setStyleName("button-panel");
        buttonPanel.getElement().getStyle().clearPosition();
        Button select = new Button(LocalizeUtil.get(CHANGE_BUTTON_KEY, CHANGE_BUTTON));
        select.setStyleName("lightButton");
        select.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Application.getInstance().setCurrentLocale(selectedLocale);
                final ActionConfig actionConfig = new ActionConfig();
                actionConfig.setDirtySensitivity(false);
                actionConfig.setImmediate(true);
//                //TODO:
                final LanguageActionContext context = new LanguageActionContext();
                context.setActionConfig(actionConfig);
                context.setLocale(selectedLocale);
                final Action action = ComponentRegistry.instance.get(LanguageActionContext.COMPONENT_NAME);
                action.setInitialContext(context);
                action.perform();
                hide();
            }
        });
        buttonPanel.add(select);
        Button cancel = new Button(LocalizeUtil.get(CANCEL_BUTTON_KEY, CANCEL_BUTTON));
        cancel.setStyleName("darkButton");
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                LanguagePopup.this.hide();
            }
        });
        buttonPanel.add(cancel);
        langPanel.add(radioButtonPanel);
        container.add(langPanel);
        container.add(buttonPanel);
        this.add(container);
        this.show();
    }


    private class RadioButtonClickHandler implements ClickHandler {
        private LanguageConfig langConfig;

        private RadioButtonClickHandler(LanguageConfig langConfig) {
            this.langConfig = langConfig;
        }

        @Override
        public void onClick(ClickEvent event) {
            selectedLocale = langConfig.getName();
        }
    }

}
