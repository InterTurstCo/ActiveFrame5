package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.LanguageConfig;
import ru.intertrust.cm.core.config.SettingsPopupConfig;
import ru.intertrust.cm.core.config.ThemeConfig;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.action.Action;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.model.action.system.ResetAllSettingsActionContext;
import ru.intertrust.cm.core.gui.model.action.system.ResetPluginSettingsActionContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ru.intertrust.cm.core.config.localization.LocalizationKeys.*;
import static ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants.*;

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
        if (settingsPopupConfig == null) {
            return;
        }

        this.setStyleName("setting-popup-panel");

        AbsolutePanel header = new AbsolutePanel();
        header.setStyleName("srch-corner");
        final VerticalPanel body = new VerticalPanel();
        AbsolutePanel container = new AbsolutePanel();
        container.setStyleName("settings-popup");
        container.getElement().getStyle().clearOverflow();
        Map<String, ThemeConfig> themeMap = GlobalThemesManager.getThemeNameImageMap();
        if(themeMap != null){
            body.add(createMenuItem(LocalizeUtil.get(CHOOSE_THEME_KEY, CHOOSE_THEME), "menuImage chooseTheme",
                    new ThemePopupDomHandler(themeMap)));
        }
        if (settingsPopupConfig.getLanguagesConfig() != null) {
            List<LanguageConfig> languageConfigs = settingsPopupConfig.getLanguagesConfig().getLanguageConfigs();
            Map<String, LanguageConfig> languageMap = new LinkedHashMap<>();
            for (LanguageConfig languageConfig : languageConfigs) {
                languageMap.put(languageConfig.getName(), languageConfig);
            }
            if (!languageMap.isEmpty()) {
                body.add(createMenuItem(LocalizeUtil.get(CHOOSE_LANG_KEY, CHOOSE_LANG), "menuImage chooseTheme",
                        new LocalePopupDomHandler(languageMap)));
            }
        }
        body.add(createMenuItem(LocalizeUtil.get(RESET_SETTINGS_KEY, RESET_SETTINGS), "menuImage resetSettings",new ResetPluginSettingDomHandler()));
        body.add(createMenuItem(LocalizeUtil.get(RESET_ALL_SETTINGS_KEY, RESET_ALL_SETTINGS), "menuImage resetAllSettings",new ResetAllSettingDomHandler()));

        container.add(header);
        container.add(body);
        this.add(container);
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

    private class LocalePopupDomHandler implements ClickHandler{
        private Map<String, LanguageConfig> languageConfigMap;

        private LocalePopupDomHandler(Map<String, LanguageConfig> languageConfigMap) {
            this.languageConfigMap = languageConfigMap;
        }

        @Override
        public void onClick(ClickEvent event) {
            LanguagePopup localePopup = new LanguagePopup(languageConfigMap);
            localePopup.center();
        }
    }
}
