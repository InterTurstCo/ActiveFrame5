package ru.intertrust.cm.core.gui.impl.client.panel;

import java.util.Map;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
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

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.05.14
 *         Time: 10:25
 */
public class SettingsPopup extends PopupPanel{
    private static final String SETTING_ITEM_STYLE = "settings-item";

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
            body.add(createThemePopup());
            body.add(createResetPluginSettings());
            body.add(createResetAllSettings());

            container.add(header);
            container.add(body);
            this.add(container);
        }
    }

    private Widget createThemePopup() {
        final Map<String, ThemeConfig> themeMap = GlobalThemesManager.getThemeNameImageMap();
        final AbsolutePanel selectTheme;
        if (themeMap != null) {
            selectTheme = new AbsolutePanel();
            Label label = new Label("Выбрать тему");
            selectTheme.add(label);
            selectTheme.setStyleName(SETTING_ITEM_STYLE);
            selectTheme.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ThemePopup themePopup = new ThemePopup(themeMap);
                    themePopup.center();
                }
            }, ClickEvent.getType());
        } else {
            selectTheme = null;
        }
        return selectTheme;
    }

    private Widget createResetPluginSettings() {
        final AbsolutePanel result = new AbsolutePanel();
        result.add(new Label("Сбросить настройки"));
        result.setStyleName(SETTING_ITEM_STYLE);
        result.addDomHandler(new ResetPluginSettingDomHandler(), ClickEvent.getType());
        return result;
    }

    private Widget createResetAllSettings() {
        final AbsolutePanel result = new AbsolutePanel();
        result.add(new Label("Сбросить все настройки"));
        result.setStyleName(SETTING_ITEM_STYLE);
        result.addDomHandler(new ResetAllSettingDomHandler(), ClickEvent.getType());
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
            final Action action = ComponentRegistry.instance.get(ResetAllSettingsActionContext.COMPONENT_NAME);
            action.setInitialContext(actionContext);
            action.perform();
            SettingsPopup.this.hide();
        }
    }
}
