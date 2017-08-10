package ru.intertrust.cm.core.gui.impl.client.plugins.globalcache;

import ru.intertrust.cm.core.config.gui.form.widget.buttons.ButtonConfig;
import ru.intertrust.cm.core.gui.impl.client.form.widget.buttons.ConfiguredButton;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 21.10.2015
 */
public class GlobalCacheControlUtils {
    public static final String STYLE_TOP_MENU_BUTTONS = "topMenuButtonsWrapper";
    public static final String STYLE_TOP_MENU_BUTTON_TEXT = "topMenuButtonText";
    public static final String STYLE_TOP_MENU_BUTTON = "topMenuButton";
    public static final String STYLE_CONTROL_PANEL = "bildControlPanel";
    public static final String STAT_REFRESH = "Обновить статистику";
    public static final String STAT_APPLY = "Применить";
    public static final String STAT_RESET = "Сбросить статистику";
    public static final String STAT_HOURLY_RESET = "Сбросить часовую статистику";
    public static final String STAT_CLEAR_CACHE = "Очистить кэш";
    public static final String BTN_IMG_REFRESH = "images/global-cache-control/iconRefresh.png";
    public static final String BTN_IMG_APPLY = "images/global-cache-control/iconApply.png";
    public static final String BTN_IMG_RESET = "images/global-cache-control/iconUndo.png";
    public static final String BTN_IMG_CLEAR = "images/global-cache-control/iconTrash.png";
    public static final String LBL_PANEL_STAT = "Статистика";
    public static final String LBL_PANEL_CONTROL = "Управление";
    public static final String LBL_CONTROL_PANEL_CACHE_ACTIVE = "Включить кэш: ";
    public static final String LBL_CONTROL_PANEL_EXPANDED_STAT = "Расширенная статистика: ";
    public static final String LBL_CONTROL_PANEL_DEBUG_MODE = "Режим отладки: ";
    public static final String LBL_CONTROL_PANEL_MODE = "Режим работы: ";
    public static final String LBL_CONTROL_PANEL_MAX_SIZE = "Максимальный размер: ";
    public static final String LBL_SHORT_STAT_SIZE = "Размер: ";
    public static final String LBL_SHORT_STAT_FREE = "Свободно: ";
    public static final String LBL_SHORT_STAT_HITS = "Попадания: ";
    public static final String WAIT_LOCK = "Ожидание блокировки, мс: ";
    public static final String MSG_SETTINGS_APPLYED = "Настройки применены";
    public static final String MSG_CONTROL_WARNING = "Включать кэш или менять режим его работы рекомендуется настройкой server.properties и перезагрузкой сервера. При смене этих параметров \"на лету\", во избежание попадания некорретных данных в кэш, его реальное включение производится в течение 1 минуты, однако 100% гарантию это не даёт, и рекомендуется к использованию исключительно в целях разработки.";




    /**
     * Создать кнопку с задаными названием и стилем
     * @param btnText
     * @param btnImage
     * @return
     */
    public static ConfiguredButton createButton(String btnText, String btnImage){
        ConfiguredButton result = new GlobalCacheControlConfiguredButton(createButtonConfig(btnImage, btnText));
        return result;
    }

    public static ButtonConfig createButtonConfig(String image, String text){
        ButtonConfig buttonConfig = new ButtonConfig();
        buttonConfig.setDisplay(true);
        buttonConfig.setText(text);
        buttonConfig.setImage(image);
        return buttonConfig;
    }
}
