package ru.intertrust.cm.core.gui.impl.client.panel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.SettingsPopupConfig;
import ru.intertrust.cm.core.config.ThemeConfig;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;

import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 14.05.14
 *         Time: 10:25
 */
public class SettingsPopup extends PopupPanel{

    public SettingsPopup(SettingsPopupConfig settingsPopupConfig) {
        super(true, false);
        initPopup(settingsPopupConfig);
    }
    private void initPopup(SettingsPopupConfig settingsPopupConfig) {
        if (settingsPopupConfig != null) {
            this.addStyleName("setting-popup-panel");

            AbsolutePanel header = new AbsolutePanel();
            header.setStyleName("srch-corner");
            AbsolutePanel body = new AbsolutePanel();
            AbsolutePanel container = new AbsolutePanel();
            container.setStyleName("settings-popup");
            container.getElement().getStyle().clearOverflow();
            initThemePopup(body);

            container.add(header);
            container.add(body);
            this.add(container);
        }
    }

    private void initThemePopup(Panel container){
        final Map<String, ThemeConfig> themeMap = GlobalThemesManager.getThemeNameImageMap();
        if(themeMap != null) {
            AbsolutePanel selectTheme = new AbsolutePanel();
            Label label = new Label("Выбрать тему");
            selectTheme.add(label);
            selectTheme.setStyleName("settings-item");
            selectTheme.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ThemePopup themePopup = new ThemePopup(themeMap);
                    themePopup.center();
                }
            }, ClickEvent.getType());
            container.add(selectTheme);
        }
    }
}
