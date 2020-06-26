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
    public static final int MIN_COLUMN_WIDTH = 50;
    public static final int MIN_RESIZE_COLUMN_WIDTH = 28;
    public static final String DATE_TIME_TYPE = "datetime";
    public static final String TIMELESS_DATE_TYPE = "timelessDate";
    public static final String DATE_TIME_WITH_TIME_ZONE_TYPE = "dateTimeWithTimeZone";
    public static final String EMPTY_VALUE = "";
    public static final int CHECK_BOX_MIN_WIDTH = 35;
    public static final int CHECK_BOX_MAX_WIDTH = 28;
    public static final String HEADER_INPUT_ID_PART = "input";
    public static final String HEADER_CLEAR_BUTTON_ID_PART = "clear-button";
    public static final String HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART = "date-picker";
    public static final String LOGIN_PAGE = "Login.html";

    public static final int START_SIDEBAR_WIDTH = 110;
    public static final String FILTER_PANEL_STATE_CLOSED = "closed";
    public static final String FILTER_PANEL_STATE_OPEN = "open";
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
    public static final String CHOOSE_DATE_RANGE_LABEL = "Выбрать период";
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
    /*
        Смещение для поля ввода поиска, чтобы уменьшить его размер на случай, когда в браузере меняется масштаб
        и из-за присутствия долей пикселей в вычисленных размерах появляется погрешность, при которой новые размеры элементов
        могут не влезть в контейнер их содержащий, в следствие чего начать располагаться неверно
    */
    public static final int SEARCH_INPUT_OFFSET = 1;

    //Hyperlink form title, maybe temporary
    public static final String FORM_TITLE = "Item";

    //Linked Table Widget
    public static final String STATE_KEY = "stateKey";

    //themes folders
    public static final String DEFAULT_THEME_FOLDER = "resources/defaulttheme/";
    public static final String LIGHT_THEME_FOLDER = "resources/lighttheme/";
    public static final String LUCEM_THEME_FOLDER = "resources/lucemtheme/";
    public static final String DARK_THEME_FOLDER = "resources/darktheme/";
    public static final String TAURIKA_THEME_FOLDER = "resources/taurikatheme/";

    // Navigation styles
    public static final String LEFT_SECTION_ACTIVE_STYLE = "left-section-active";
    public static final String LEFT_SECTION_STYLE = "left-section";
    public static final String LEFT_SECTION_COLLAPSED_STYLE = "left-sectionNone";

    public static final String CENTRAL_SECTION_STYLE = "central-div-panel-test";
    public static final String CENTRAL_SECTION_FULL_SIZE_STYLE = "central-div-panel-test-full";
    public static final String CENTRAL_SECTION_RIGHT_PANEL_OPEN_STYLE = "centralPanelRightPanelOpen";
    public static final String CENTRAL_SECTION_LEFT_AND_RIGHT_PANEL_OPEN_STYLE = "centralPanelLeftAndRightPanelOpen";
    public static final String CENTRAL_SECTION_RIGHT_PANEL_OPEN_FULL_STYLE = "centralPanelRightPanelOpenFull";
    public static final String CENTRAL_SECTION_ACTIVE_STYLE = "central-div-panel-test-active";

    public static final String RIGHT_SECTION_COLLAPSED_STYLE = "stickerPanelOff";
    public static final String RIGHT_SECTION_EXPANDED_STYLE = "stickerPanelOn";
    public static final String RIGHT_SECTION_EXPANDED_FULL_STYLE = "stickerPanelOnFull";
    public static final String RIGHT_SECTION_COLLAPSED_FULL_STYLE = "stickerPanelOffFull";

    public static final String TOP_SECTION_COLLAPSED_STYLE = "header-sectionNone";
    public static final String TOP_SECTION_STYLE = "header-section";

    //alert messages
    public static final String TASK_COMPLETE_MESSAGE = "Задача успешно завершена";
    public static final String ROW_IS_DELETED_MESSAGE = "Строка удалена";
    public static final String REPORT_IS_UPLOADED_MESSAGE = "Отчет успешно загружен";
    public static final String CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE = "Поля на форме не заполнены или заполнены некорректно.\nИсправьте ошибки заполнения перед сохранением.";
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

    public static final String DEFAULT_EMBEDDED_COLLECTION_TABLE_HEIGHT = "300px";
    public static final String DEFAULT_EMBEDDED_COLLECTION_TABLE_WIDTH = "800px";

    public static final int BROWSER_INACCURACY = 1;

    public static final String CHOOSE_THEME = "Выбрать тему";
    public static final String RESET_SETTINGS = "Сбросить настройки";
    public static final String RESET_ALL_SETTINGS = "Сбросить все настройки";
    public static final String EXIT = "Выход";
    public static final String CORE_VERSION = "Версия платформы:";
    public static final String VERSION = "Версия:";
    public static final String INFO = "Справка";
    public static final String LOGIN = "Логин:";
    public static final String FIRST_NAME = "Имя:";
    public static final String LAST_NAME = "Фамилия:";
    public static final String EMAIL = "EMail:";
    public static final String ADMINISTRATOR = "Администратор";
    public static final String CHOOSE_LANG = "Выбрать язык";

    public static final String OK_BUTTON = "OK";
    public static final String CANCEL_BUTTON = "Отменить";
    public static final String CANCELLATION_BUTTON = "Отмена";
    public static final String CLOSE_BUTTON = "Закрыть";
    public static final String OPEN_IN_FULL_WINDOW = "Открыть в полном окне";
    public static final String DONE_BUTTON = "Готово";
    public static final String SAVE_BUTTON = "Сохранить";
    public static final String CHANGE_BUTTON = "Изменить";
    public static final String EDIT_BUTTON = "Редактировать";
    public static final String FIND_BUTTON = "Найти";
    public static final String CONTINUE_BUTTON = "Продолжить";

    public static final String SEARCH = "Поиск";
    public static final String EMPTY_TABLE = "Результаты отсутствуют";
    public static final String CLEAR_FORM = "Очистить форму";
    public static final String SIZE_ACTION_TOOLTIP = "Распахнуть/Свернуть";
    public static final String FAVORITE_ACTION_TOOLTIP = "Показать/Скрыть избранное";
    public static final String FILTER_TOOLTIP = "Отобразить/Скрыть фильтры";
    public static final String COLUMNS_DISPLAY_TOOLTIP = "Отображение видимых колонок";
    public static final String COLUMNS_WIDTH_TOOLTIP = "Отобразить все колонки на экране";
    public static final String NO_NEW_NOTIFICATIONS = "Новых уведомлений нет";
    public static final String ADD_CONFIG_FILES = "Добавьте файлы конфигурации:";
    public static final String SELECT_CONFIG_TYPE = "Выберите тип конфигурации";
    public static final String CONFIG_FILES_TYPE = "Тип конфигурации:";
    public static final String ADD_REPORT_FILES = "Добавьте файлы шаблона отчета:";

    public static final String MONTH_SWITCH_BUTTON = "Задачи дня";
    public static final String WEEK_SWITCH_BUTTON = "Суббота, Воскресенье";

   /* public static final String[] MONTHS = {"JanuaryMonthShortStandalone", "FebruaryMonthShortStandalone", "MarchMonthShortStandalone",
            "AprilMonthShortStandalone", "MayMonthShortStandalone", "JunMonthShortStandalone", "JulyMonthShortStandalone",
            "AugustMonthShortStandalone", "SeptemberMonthShortStandalone", "OctoberMonthShortStandalone",
            "NovemberMonthShortStandalone", "DecemberMonthShortStandalone"};
    public static final String[] WEEK_DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
            "Saturday"};*/

    public static final String EXECUTION_ACTION_ERROR = "Ошибка выполнения действия: ";
    public static final String DATE_FORMAT_ERROR = "Ошибка в формате даты: ";
    public static final String OBJECT_NOT_SAVED = "Объект должен быть сохранен перед выполнением операции";

    public static final String DIGITAL_SIGNATURE = "ЭЦП";
    public static final String COULD_NOT_EXECUTE_ACTION_DURING_UPLOADING_FILES = "Could not execute action during uploading files";
    public static final String MOVE_COLUMN_HINT = "Нажмите и потяните для перемещения колонки";
    public static final String RESIZE_COLUMN_HINT = "Нажмите и потяните для изменения размера колонки";
    public static final String ADD_PLUGIN_FILES = "Добавьте файлы плагинов:";    
    
    private BusinessUniverseConstants() {
    }
}
