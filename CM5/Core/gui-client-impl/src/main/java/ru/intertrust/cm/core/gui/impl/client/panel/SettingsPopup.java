package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.config.SettingsPopupConfig;
import ru.intertrust.cm.core.config.ThemeConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.model.action.system.ResetAllSettingsActionContext;
import ru.intertrust.cm.core.gui.model.action.system.ResetPluginSettingsActionContext;

import java.util.Map;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.05.14
 *         Time: 10:25
 */
public class SettingsPopup extends PopupPanel{
    private static final String SETTING_ITEM_STYLE = "settingsItem";

    public SettingsPopup(SettingsPopupConfig settingsPopupConfig) {
        super(true, false);
        initPopup(settingsPopupConfig);
    }
    private void initPopup(SettingsPopupConfig settingsPopupConfig) {
        if (settingsPopupConfig != null) {
            this.setStyleName("setting-popup-panel");

            AbsolutePanel header = new AbsolutePanel();
            header.setStyleName("srch-corner");
            final VerticalPanel body = new VerticalPanel();
            AbsolutePanel container = new AbsolutePanel();
            container.setStyleName("settings-popup");
            container.getElement().getStyle().clearOverflow();
            Map<String, ThemeConfig> themeMap = GlobalThemesManager.getThemeNameImageMap();
            if(themeMap != null){
                body.add(createMenuItem("Выбрать тему", "menuImage chooseTheme",new ThemePopupDomHandler(themeMap)));
            }
            body.add(createMenuItem("Сбросить настройки", "menuImage resetSettings",new ResetPluginSettingDomHandler()));
            body.add(createMenuItem("Сбросить все настройки", "menuImage resetAllSettings",new ResetAllSettingDomHandler()));

            container.add(header);
            container.add(body);
            this.add(container);
        }
    }


    private Widget createMenuItem(String text, String imageStyleClass,ClickHandler handler){
        final AbsolutePanel result = new AbsolutePanel();
        Panel imagePanel = new AbsolutePanel();
        imagePanel.addStyleName(imageStyleClass);
        result.add(imagePanel);
        result.add(new Label(text));
        result.setStyleName(SETTING_ITEM_STYLE);
        result.addDomHandler(handler, ClickEvent.getType());
        return result;
    }

    private static ActionConfig createActionConfig() {
        final ActionConfig result = new ActionConfig();
        result.setImmediate(true);
        result.setDirtySensitivity(false);
        return result;
    }

    private class ResetPluginSettingDomHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            final ActionConfig actionConfig = createActionConfig();
            final ResetPluginSettingsActionContext actionContext = new ResetPluginSettingsActionContext();
            actionContext.setActionConfig(actionConfig);
            actionContext.setLink(Application.getInstance().getHistoryManager().getLink());
            final Action action = ComponentRegistry.instance.get(ResetPluginSettingsActionContext.COMPONENT_NAME);
            action.setInitialContext(actionContext);
            action.perform();
            SettingsPopup.this.hide();
        }
    }

    private class ResetAllSettingDomHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            final ActionConfig actionConfig = createActionConfig();
            final ResetAllSettingsActionContext actionContext = new ResetAllSettingsActionContext();
            actionContext.setActionConfig(actionConfig);
            actionContext.setDefaultTheme(GlobalThemesManager.THEME_DEFAULT);
            final Action action = ComponentRegistry.instance.get(ResetAllSettingsActionContext.COMPONENT_NAME);
            action.setInitialContext(actionContext);
            action.perform();

            SettingsPopup.this.hide();
        }
    }

    private class ThemePopupDomHandler implements ClickHandler{
        private Map<String, ThemeConfig> themeMap;

        private ThemePopupDomHandler(Map<String, ThemeConfig> themeMap) {
            this.themeMap = themeMap;
        }

        @Override
        public void onClick(ClickEvent event) {
            ThemePopup themePopup = new ThemePopup(themeMap);
            themePopup.center();
        }
    }
}
