package ru.intertrust.cm.core.gui.impl.client.util;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 31.01.14
 *         Time: 13:15
 */
public class BusinessUniverseConstants {
    // Navigation tree constants
    public static final String TREE_ITEM_NAME = "name";
    public static final String TREE_ITEM_ORIGINAL_TEXT = "originalText";
    public static final String TREE_ITEM_PLUGIN_CONFIG = "pluginConfig";


    // gui constants
    public static final String CHECK_BOX_COLUMN_NAME = "checkBox";
    public static final String DESCEND_ARROW = "↓";
    public static final String ASCEND_ARROW = "↑";
    public static final String DISPLAY_STYLE_BlOCK = "table";

    public static final String FOOTER_SHORT = "footer-short";
    public static final String FOOTER_LONG = "footer-long";

    public static final int MAX_COLUMN_WIDTH = 9999;
    public static final int MIN_COLUMN_WIDTH = 230;
    public static final int MIN_RESIZE_COLUMN_WIDTH = 28;
    public static final String DATE_TIME_TYPE = "datetime";
    public static final String TIMELESS_DATE_TYPE = "timelessDate";
    public static final String DATE_TIME_WITH_TIME_ZONE_TYPE = "dateTimeWithTimeZone";
    public static final String EMPTY_VALUE = "";
    public static final int CHECK_BOX_MIN_WIDTH = 35;
    public static final int CHECK_BOX_MAX_WIDTH = 35;
    public static final String HEADER_INPUT_ID_PART = "input";
    public static final String HEADER_CLEAR_BUTTON_ID_PART = "clear-button";
    public static final String HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART = "date-picker";
    public static final String LOGIN_PAGE = "Login.html";

    public static final int START_SIDEBAR_WIDTH = 110;
    public static final String CLOSED = "closed";
    public static final String OPEN = "open";
    public static final String UNDEFINED_COLLECTION_NAME = "undefined";
    //Date selectors labels
    public static final String TODAY_LABEL = "Сегодня";
    public static final String LAST_WEEK_LABEL = "Прошлая неделя";
    public static final String LAST_YEAR_LABEL = "Прошлый год";
    public static final String YESTERDAY_LABEL = "Вчера";
    public static final String CURRENT_WEEK_LABEL = "Текущая неделя";
    public static final String NEXT_WEEK_LABEL = "Следущая неделя";
    public static final String NEXT_YEAR_LABEL = "Следущий год";
    public static final String TOMORROW_LABEL = "Завтра";
    public static final String CHOSE_DATE_LABEL = "Выбрать дату";
    public static final String CHOSE_DATE_RANGE_LABEL = "Выбрать период";
    public static final String DATETIME_PICKER_BUTTON = "Готово";
    public static final String ZERO_STRING = "0";

    public static final String FOR_TODAY_LABEL = "За сегодня";
    public static final String FOR_LAST_WEEK_LABEL = "За неделю";
    public static final String FOR_LAST_YEAR_LABEL = "За год";
    public static final String FOR_YESTERDAY_LABEL = "За вчера";

    //Collection header
    public static final int RESIZE_HANDLE_WIDTH = 14;
    public static final int MOVE_HANDLE_WIDTH = 14;
    public static final int CLEAR_BUTTON_WIDTH = 28;
    public static final int FILTER_CONTAINER_MARGIN = 20;

    //Hyperlink form title, maybe temporary
    public static final String FORM_TITLE = "Item";

    //Linked Table Widget
    public static final String STATE_KEY = "stateKey";

    //themes folders
    public static final String DEFAULT_THEME_FOLDER = "resources/defaulttheme/";
    public static final String LIGHT_THEME_FOLDER = "resources/lighttheme/";
    public static final String LUCEM_THEME_FOLDER = "resources/lucemtheme/";
    public static final String DARK_THEME_FOLDER = "resources/darktheme/";

    // Navigation styles
    public static final String LEFT_SECTION_ACTIVE_STYLE = "left-section-active";
    public static final String LEFT_SECTION_STYLE = "left-section";

    public static final String CENTRAL_SECTION_STYLE = "central-div-panel-test";
    public static final String CENTRAL_SECTION_ACTIVE_STYLE = "central-div-panel-test-active";

    //alert messages
    public static final String TASK_COMPLETE_MESSAGE = "Задача успешно завершена";
    public static final String ROW_IS_DELETED_MESSAGE = "Строка удалена";
    public static final String REPORT_IS_UPLOADED_MESSAGE = "Репорт успешно загружен";
    public static final String CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE = "Исправьте ошибки перед сохранением";
    public static final String SAVED_MESSAGE = "Успешно сохранено";
    public static final String EVENT_IS_SENT_MESSAGE = "Событие отправлно";
    public static final String SYSTEM_EXCEPTION_MESSAGE = "Ошибка системы";
    public static final String BUSINESS_UNIVERSE_CONTEXT_EXCEPTION_MESSAGE = "BusinessUniverseContext не получил конфигурацию для поиска";
    public static final String DONE_SUCCESSFULLY_MESSAGE = "Успешно выполнено";
    public static final String PROCESS_IS_STARTED_MESSAGE = "Процесс запущен";
    public static final String COULD_NOT_SAVE_USER_SETTINGS_MESSAGE = "Невозможно сохранить настройки пользователя";
    public static final String LOGOUT_ERROR_MESSAGE = "Ошибка выхода из приложения";
    public static final String EXTENDED_SEARCH_ERROR_MESSAGE = "Ошибка расширенного поиска ";
    public static final String WRONG_DATE_FORMAT_ERROR_MESSAGE = "Неверный формат времени! Попробуйте ";
    public static final String DATA_IS_NOT_SAVED_CONFIRM_MESSAGE= "Изменения данных не сохранены.\nПродолжить выполнение команды ?";

    private BusinessUniverseConstants() {
    }
}
