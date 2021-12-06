package ru.intertrust.cm.core.config.localization;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 03.03.2015
 *         Time: 15:44
 */
public class LocalizationKeys {

    private LocalizationKeys() {
    }  //non-instantiable

    public static final String FOR_TODAY_LABEL_KEY = "ForToday";
    public static final String FOR_LAST_WEEK_LABEL_KEY = "ForLastWeek";
    public static final String FOR_LAST_YEAR_LABEL_KEY = "ForLastYear";
    public static final String FOR_YESTERDAY_LABEL_KEY = "ForYesterday";
    public static final String CHOOSE_DATE_RANGE_LABEL_KEY = "ChoosePeriod";

    public static final String TASK_COMPLETE_MESSAGE_KEY = "TaskComplete";
    public static final String ROW_IS_DELETED_MESSAGE_KEY = "RowDeleted";
    public static final String REPORT_IS_UPLOADED_MESSAGE_KEY = "ReportUploaded";
    public static final String REPORTS_ARE_UPLOADED_MESSAGE_KEY = "ReportsUploaded";
    public static final String CORRECT_VALIDATION_ERRORS_BEFORE_SAVING_MESSAGE_KEY = "CorrectValidationErrors";
    public static final String SAVED_MESSAGE_KEY = "Saved";
    public static final String EVENT_IS_SENT_MESSAGE_KEY = "MessageSent";
    public static final String SYSTEM_EXCEPTION_MESSAGE_KEY = "SystemError";
    public static final String BUSINESS_UNIVERSE_CONTEXT_EXCEPTION_MESSAGE_KEY = "BusinessUniverseContextError";
    public static final String DONE_SUCCESSFULLY_MESSAGE_KEY = "DoneSuccessfully";
    public static final String PROCESS_IS_STARTED_MESSAGE_KEY = "ProcessStarted";
    public static final String COULD_NOT_SAVE_USER_SETTINGS_MESSAGE_KEY = "CannotSaveUserSettings";
    public static final String LOGOUT_ERROR_MESSAGE_KEY = "LogoutError";
    public static final String EXTENDED_SEARCH_ERROR_MESSAGE_KEY = "ExtendedSearchError";
    public static final String WRONG_DATE_FORMAT_ERROR_MESSAGE_KEY = "WrongDateFormat";
    public static final String DATA_IS_NOT_SAVED_CONFIRM_MESSAGE = "DataNotSavedConfirm";

    public static final String CHOOSE_THEME_KEY = "ChooseTheme";
    public static final String RESET_SETTINGS_KEY = "ResetSettings";
    public static final String RESET_ALL_SETTINGS_KEY = "ResetAllSettings";
    public static final String EXIT_KEY = "Exit";
    public static final String CORE_VERSION_KEY = "CoreVersion";
    public static final String VERSION_KEY = "Version";
    public static final String INFO_KEY = "Info";
    public static final String LOGIN_KEY = "Login";
    public static final String FIRST_NAME_KEY = "FirstName";
    public static final String LAST_NAME_KEY = "LastName";
    public static final String EMAIL_KEY = "EMail";
    public static final String ADMINISTRATOR_KEY = "Administrator";
    public static final String CHOOSE_LANG_KEY = "ChooseLanguage";

    public static final String OK_BUTTON_KEY = "OkButton";
    public static final String CANCEL_BUTTON_KEY = "CancelButton";
    public static final String CANCELLATION_BUTTON_KEY = "CancellationButton";
    public static final String CLOSE_BUTTON_KEY = "CloseButton";
    public static final String OPEN_IN_FULL_WINDOW_KEY = "OpenInFullWindowButton";
    public static final String DONE_BUTTON_KEY = "DoneButton";
    public static final String SAVE_BUTTON_KEY = "SaveButton";
    public static final String CHANGE_BUTTON_KEY = "ChangeButton";
    public static final String EDIT_BUTTON_KEY = "EditButton";
    public static final String FIND_BUTTON_KEY = "FindButton";
    public static final String CONTINUE_BUTTON_KEY = "ContinueButton";

    public static final String SEARCH_KEY = "Search";
    public static final String EMPTY_TABLE_KEY = "NoResults";
    public static final String CLEAR_FORM_KEY = "ClearForm";

    public static final String SIZE_ACTION_TOOLTIP_KEY = "SizeActionTooltip";
    public static final String FAVORITE_ACTION_TOOLTIP_KEY = "FavoriteActionTooltip";
    public static final String FILTER_TOOLTIP_KEY = "FilterTooltip";
    public static final String COLUMNS_DISPLAY_TOOLTIP_KEY = "ColumnsDisplayTooltip";
    public static final String COLUMNS_WIDTH_TOOLTIP_KEY = "ColumnsWidthTooltip";

    public static final String NO_NEW_NOTIFICATIONS_KEY = "NoNewNotifications";
    public static final String ADD_CONFIG_FILES_KEY = "AddConfigFiles";
    public static final String SELECT_CONFIG_TYPE_KEY = "SelectConfigType";
    public static final String CONFIG_FILES_TYPE_KEY = "ConfigFilesType";
    public static final String ADD_REPORT_FILES_KEY = "AddReportFiles";
    public static final String ADD_REPORTS_PACKAGE_KEY = "AddReportsPackage";

    public static final String MONTH_SWITCH_BUTTON_KEY = "MonthSwitchButton";
    public static final String WEEK_SWITCH_BUTTON_KEY = "WeekSwitchButton";

    public static final String EXECUTION_ACTION_ERROR_KEY = "ActionExecutionError";
    public static final String DATE_FORMAT_ERROR_KEY = "DateFormatError";

    public static final String SERVER_VALIDATION_EXCEPTION = "ServerValidationException";
    public static final String SYSTEM_EXCEPTION = "SystemException";

    public static final String GUI_EXCEPTION_COMMAND_NOT_FOUND = "GuiException.CommandNotFound";
    public static final String GUI_EXCEPTION_COMMAND_CALL = "GuiException.CommandCallError";
    public static final String GUI_EXCEPTION_COMMAND_EXECUTION = "GuiException.CommandExecutionError";
    public static final String GUI_EXCEPTION_MULTIPLE_FIELDPATHS = "GuiException.MultipleFieldPaths";
    public static final String GUI_EXCEPTION_SINGLE_FIELDPATH = "GuiException.SingleFieldPath";
    public static final String GUI_EXCEPTION_OBJECT_NOT_SAVED = "GuiException.ObjectNotSaved";
    public static final String GUI_EXCEPTION_NO_PROCESS_TYPE = "GuiException.NoProcessType";
    public static final String GUI_EXCEPTION_NO_PROCESS_NAME = "GuiException.NoProcessName";
    public static final String GUI_EXCEPTION_PROCESS_NOT_SUPPORTED = "GuiException.ProcessNotSupported";
    public static final String GUI_EXCEPTION_REF_PATH_NOT_SUPPORTED = "GuiException.RefPathNotSupported";
    public static final String GUI_EXCEPTION_UNKNOWN_URL = "GuiException.UnknownUrl";
    public static final String GUI_EXCEPTION_MANY_DEFAULT_FORMS = "GuiException.ManyDefaultForms";
    public static final String GUI_EXCEPTION_OBJECT_NOT_EXIST = "GuiException.ObjectNotExist";
    public static final String GUI_EXCEPTION_REPORT_FORM_ERROR = "GuiException.ReportFormError";
    public static final String GUI_EXCEPTION_REPORT_NAME_NOT_FOUND = "GuiException.ReportNameNotFound";
    public static final String GUI_EXCEPTION_WIDGET_ID_NOT_FOUND = "GuiException.WidgetIdNotFound";
    public static final String GUI_EXCEPTION_SEARCH = "GuiException.SearchError";
    public static final String GUI_EXCEPTION_COLLECTION_VIEW = "GuiException.CollectionViewError";
    public static final String GUI_EXCEPTION_HIERARCH_COLLECTION = "GuiException.HierarchCollection";
    public static final String GUI_EXCEPTION_SORT_FIELD_NOT_FOUND = "GuiException.SortingFieldNotFound";
    public static final String GUI_EXCEPTION_VERSION_ERROR = "GuiException.VersionError";

    public static final String GUI_EXCEPTION_FILE_IS_UPLOADING_KEY = "GuiException.FileIsUploading";

    public static final String USER_NAME_KEY = "UserName";
    public static final String PSSWD_KEY = "Password";
    public static final String SIGN_ON_KEY = "SignOn";
    public static final String REMEMBER_ME_KEY = "RememberMe";
    public static final String AUTHORIZATION_ERROR_KEY = "AuthorizationError";
    public static final String AUTHORIZATION_WRONG_PSW_ERROR_KEY = "WrongPswError";
    public static final String AUTHORIZATION_CONNECTION_ERROR_KEY = "NoConnectionError";

    public static final String OBJECT_NOT_SAVED_KEY = "ObjectMustBeSaved";

    public static final String DIGITAL_SIGNATURE_KEY = "DigitalSignature";

    public static final String MOVE_COLUMN_HINT_KEY = "MoveColumnHint";
    public static final String RESIZE_COLUMN_HINT_KEY = "ResizeColumnHint";

    public static final Map<String, String> validationMessages = new HashMap<>(); //<key, defaultValue>
    static {
        validationMessages.put("validate.not-empty", "Поле ${field-name} не должно быть пустым!");
        validationMessages.put("validate.integer", "'${value}' должно быть целым!");
        validationMessages.put("validate.decimal", "'${value}' должно быть десятичным!");
        validationMessages.put("validate.positive-int", "'${value}' должно быть целым положительным!");
        validationMessages.put("validate.positive-dec", "'${value}' должно быть десятичным положительным!");
        validationMessages.put("validate.unique-field", "Поле ${field-name} со значением '${value}' уже существует!");
        validationMessages.put("validate.pattern", "Поле ${field-name} должно соответствовать шаблону ${pattern}!");
        validationMessages.put("validate.length.not-equal", "Длина поля ${field-name} должна быть равна ${length}");
        validationMessages.put("validate.length.too-small", "Длина поля ${field-name} не может быть меньше чем${min-length}");
        validationMessages.put("validate.length.too-big", "Длина поля ${field-name} не может быть больше чем ${max-length}");
        validationMessages.put("validate.range.too-small","Значение поля ${field-name} не может быть меньше чем ${range-start}" );
        validationMessages.put("validate.range.too-big", "Значение поля ${field-name} не может быть больше чем ${range-end}");
        validationMessages.put("validate.precision", "Значение поля ${field-name} должно иметь точность ${precision}");
        validationMessages.put("validate.scale", "Значение поля ${field-name} должно иметь ${scale} знаков после запятой");
    }
    public static final String JANUARY_MONTH_SHORT_STANDALONE_KEY = "JanuaryMonthShortStandalone";
    public static final String FEBRUARY_MONTH_SHORT_STANDALONE_KEY = "FebruaryMonthShortStandalone";
    public static final String MARCH_MONTH_SHORT_STANDALONE_KEY = "MarchMonthShortStandalone";
    public static final String APRIL_MONTH_SHORT_STANDALONE_KEY = "AprilMonthShortStandalone";
    public static final String MAY_MONTH_SHORT_STANDALONE_KEY = "MayMonthShortStandalone";
    public static final String JUN_MONTH_SHORT_STANDALONE_KEY = "JunMonthShortStandalone";
    public static final String JULY_MONTH_SHORT_STANDALONE_KEY = "JulyMonthShortStandalone";
    public static final String AUGUST_MONTH_SHORT_STANDALONE_KEY = "AugustMonthShortStandalone";
    public static final String SEPTEMBER_MONTH_SHORT_STANDALONE_KEY = "SeptemberMonthShortStandalone";
    public static final String OCTOBER_MONTH_SHORT_STANDALONE_KEY = "OctoberMonthShortStandalone";
    public static final String NOVEMBER_MONTH_SHORT_STANDALONE_KEY = "NovemberMonthShortStandalone";
    public static final String DECEMBER_MONTH_SHORT_STANDALONE_KEY = "DecemberMonthShortStandalone";
    public static final String[] MONTHS = {JANUARY_MONTH_SHORT_STANDALONE_KEY , FEBRUARY_MONTH_SHORT_STANDALONE_KEY,
            MARCH_MONTH_SHORT_STANDALONE_KEY, APRIL_MONTH_SHORT_STANDALONE_KEY, MAY_MONTH_SHORT_STANDALONE_KEY,
            JUN_MONTH_SHORT_STANDALONE_KEY, JULY_MONTH_SHORT_STANDALONE_KEY,
            AUGUST_MONTH_SHORT_STANDALONE_KEY, SEPTEMBER_MONTH_SHORT_STANDALONE_KEY, OCTOBER_MONTH_SHORT_STANDALONE_KEY,
            NOVEMBER_MONTH_SHORT_STANDALONE_KEY, DECEMBER_MONTH_SHORT_STANDALONE_KEY};

    public static final String SUNDAY_KEY = "Sunday";
    public static final String MONDAY_KEY = "Monday";
    public static final String TUESDAY_KEY = "Tuesday";
    public static final String WEDNESDAY_KEY = "Wednesday";
    public static final String THURSDAY_KEY = "Thursday";
    public static final String FRIDAY_KEY = "Friday";
    public static final String SATURDAY_KEY = "Saturday";
    public static final String[] WEEK_DAYS = {SUNDAY_KEY, MONDAY_KEY, TUESDAY_KEY, WEDNESDAY_KEY, THURSDAY_KEY, FRIDAY_KEY,
            SATURDAY_KEY};

    public static final String SUNDAY_FIRST_CAPITAL_KEY = "SundayFirstCapital";
    public static final String MONDAY_FIRST_CAPITAL_KEY = "MondayFirstCapital";
    public static final String TUESDAY_FIRST_CAPITAL_KEY = "TuesdayFirstCapital";
    public static final String WEDNESDAY_FIRST_CAPITAL_KEY = "WednesdayFirstCapital";
    public static final String THURSDAY_FIRST_CAPITAL_KEY = "ThursdayFirstCapital";
    public static final String FRIDAY_FIRST_CAPITAL_KEY = "FridayFirstCapital";
    public static final String SATURDAY_FIRST_CAPITAL_KEY = "SaturdayFirstCapital";

    public static final String SUNDAY_FULL_KEY = "SundayFull";
    public static final String MONDAY_FULL_KEY = "MondayFull";
    public static final String TUESDAY_FULL_KEY = "TuesdayFull";
    public static final String WEDNESDAY_FULL_KEY = "WednesdayFull";
    public static final String THURSDAY_FULL_KEY = "ThursdayFull";
    public static final String FRIDAY_FULL_KEY = "FridayFull";
    public static final String SATURDAY_FULL_KEY = "SaturdayFull";

    public static final String SUNDAY_FULL_STANDALONE_KEY = "SundayFullStandalone";
    public static final String MONDAY_FULL_STANDALONE_KEY = "MondayFullStandalone";
    public static final String TUESDAY_FULL_STANDALONE_KEY = "TuesdayFullStandalone";
    public static final String WEDNESDAY_FULL_STANDALONE_KEY = "WednesdayFullStandalone";
    public static final String THURSDAY_FULL_STANDALONE_KEY = "ThursdayFullStandalone";
    public static final String FRIDAY_FULL_STANDALONE_KEY = "FridayFullStandalone";
    public static final String SATURDAY_FULL_STANDALONE_KEY = "SaturdayFullStandalone";

    public static final String SUNDAY_NARROW_KEY = "SundayNarrow";
    public static final String MONDAY_NARROW_KEY = "MondayNarrow";
    public static final String TUESDAY_NARROW_KEY = "TuesdayNarrow";
    public static final String WEDNESDAY_NARROW_KEY = "WednesdayNarrow";
    public static final String THURSDAY_NARROW_KEY = "ThursdayNarrow";
    public static final String FRIDAY_NARROW_KEY = "FridayNarrow";
    public static final String SATURDAY_NARROW_KEY = "SaturdayNarrow";

    public static final String SUNDAY_SHORT_KEY = "SundayShort";
    public static final String MONDAY_SHORT_KEY = "MondayShort";
    public static final String TUESDAY_SHORT_KEY = "TuesdayShort";
    public static final String WEDNESDAY_SHORT_KEY = "WednesdayShort";
    public static final String THURSDAY_SHORT_KEY = "ThursdayShort";
    public static final String FRIDAY_SHORT_KEY = "FridayShort";
    public static final String SATURDAY_SHORT_KEY = "SaturdayShort";

    public static final String SUNDAY_SHORT_STANDALONE_KEY = "SundayShortStandalone";
    public static final String MONDAY_SHORT_STANDALONE_KEY = "MondayShortStandalone";
    public static final String TUESDAY_SHORT_STANDALONE_KEY = "TuesdayShortStandalone";
    public static final String WEDNESDAY_SHORT_STANDALONE_KEY = "WednesdayShortStandalone";
    public static final String THURSDAY_SHORT_STANDALONE_KEY = "ThursdayShortStandalone";
    public static final String FRIDAY_SHORT_STANDALONE_KEY = "FridayShortStandalone";
    public static final String SATURDAY_SHORT_STANDALONE_KEY = "SaturdayShortStandalone";

    public static final String JANUARY_MONTH_SHORT_KEY = "JanuaryMonthShort";
    public static final String FEBRUARY_MONTH_SHORT_KEY = "FebruaryMonthShort";
    public static final String MARCH_MONTH_SHORT_KEY = "MarchMonthShort";
    public static final String APRIL_MONTH_SHORT_KEY = "AprilMonthShort";
    public static final String MAY_MONTH_SHORT_KEY = "MayMonthShort";
    public static final String JUN_MONTH_SHORT_KEY = "JunMonthShort";
    public static final String JULY_MONTH_SHORT_KEY = "JulyMonthShort";
    public static final String AUGUST_MONTH_SHORT_KEY = "AugustMonthShort";
    public static final String SEPTEMBER_MONTH_SHORT_KEY = "SeptemberMonthShort";
    public static final String OCTOBER_MONTH_SHORT_KEY = "OctoberMonthShort";
    public static final String NOVEMBER_MONTH_SHORT_KEY = "NovemberMonthShort";
    public static final String DECEMBER_MONTH_SHORT_KEY = "DecemberMonthShort";

    public static final String JANUARY_MONTH_NARROW_KEY = "JanuaryMonthNarrow";
    public static final String FEBRUARY_MONTH_NARROW_KEY = "FebruaryMonthNarrow";
    public static final String MARCH_MONTH_NARROW_KEY = "MarchMonthNarrow";
    public static final String APRIL_MONTH_NARROW_KEY = "AprilMonthNarrow";
    public static final String MAY_MONTH_NARROW_KEY = "MayMonthNarrow";
    public static final String JUN_MONTH_NARROW_KEY = "JunMonthNarrow";
    public static final String JULY_MONTH_NARROW_KEY = "JulyMonthNarrow";
    public static final String AUGUST_MONTH_NARROW_KEY = "AugustMonthNarrow";
    public static final String SEPTEMBER_MONTH_NARROW_KEY = "SeptemberMonthNarrow";
    public static final String OCTOBER_MONTH_NARROW_KEY = "OctoberMonthNarrow";
    public static final String NOVEMBER_MONTH_NARROW_KEY = "NovemberMonthNarrow";
    public static final String DECEMBER_MONTH_NARROW_KEY = "DecemberMonthNarrow";

    public static final String JANUARY_MONTH_FULL_STANDALONE_KEY = "JanuaryMonthFullStandalone";
    public static final String FEBRUARY_MONTH_FULL_STANDALONE_KEY = "FebruaryMonthFullStandalone";
    public static final String MARCH_MONTH_FULL_STANDALONE_KEY = "MarchMonthFullStandalone";
    public static final String APRIL_MONTH_FULL_STANDALONE_KEY = "AprilMonthFullStandalone";
    public static final String MAY_MONTH_FULL_STANDALONE_KEY = "MayMonthFullStandalone";
    public static final String JUN_MONTH_FULL_STANDALONE_KEY = "JunMonthFullStandalone";
    public static final String JULY_MONTH_FULL_STANDALONE_KEY = "JulyMonthFullStandalone";
    public static final String AUGUST_MONTH_FULL_STANDALONE_KEY = "AugustMonthFullStandalone";
    public static final String SEPTEMBER_MONTH_FULL_STANDALONE_KEY = "SeptemberMonthFullStandalone";
    public static final String OCTOBER_MONTH_FULL_STANDALONE_KEY = "OctoberMonthFullStandalone";
    public static final String NOVEMBER_MONTH_FULL_STANDALONE_KEY = "NovemberMonthFullStandalone";
    public static final String DECEMBER_MONTH_FULL_STANDALONE_KEY = "DecemberMonthFullStandalone";

    public static final String JANUARY_MONTH_FULL_KEY = "JanuaryMonthFull";
    public static final String FEBRUARY_MONTH_FULL_KEY = "FebruaryMonthFull";
    public static final String MARCH_MONTH_FULL_KEY = "MarchMonthFull";
    public static final String APRIL_MONTH_FULL_KEY = "AprilMonthFull";
    public static final String MAY_MONTH_FULL_KEY = "MayMonthFull";
    public static final String JUN_MONTH_FULL_KEY = "JunMonthFull";
    public static final String JULY_MONTH_FULL_KEY = "JulyMonthFull";
    public static final String AUGUST_MONTH_FULL_KEY = "AugustMonthFull";
    public static final String SEPTEMBER_MONTH_FULL_KEY = "SeptemberMonthFull";
    public static final String OCTOBER_MONTH_FULL_KEY = "OctoberMonthFull";
    public static final String NOVEMBER_MONTH_FULL_KEY = "NovemberMonthFull";
    public static final String DECEMBER_MONTH_FULL_KEY = "DecemberMonthFull";

    public static final String QUARTER_FIRST_SHORT_KEY = "QuarterFirstShort";
    public static final String QUARTER_SECOND_SHORT_KEY = "QuarterSecondShort";
    public static final String QUARTER_THIRD_SHORT_KEY = "QuarterThirdShort";
    public static final String QUARTER_FORTH_SHORT_KEY = "QuarterForthShort";

    public static final String QUARTER_FIRST_FULL_KEY = "QuarterFirstFull";
    public static final String QUARTER_SECOND_FULL_KEY = "QuarterSecondFull";
    public static final String QUARTER_THIRD_FULL_KEY = "QuarterThirdFull";
    public static final String QUARTER_FORTH_FULL_KEY = "QuarterForthFull";

    public static final String DATE_SEPARATOR_KEY = "DateSeparator";

    public static final String DATE_FORMAT_FULL_KEY = "DateFormatFull";
    public static final String DATE_FORMAT_LONG_KEY = "DateFormatLong";
    public static final String DATE_FORMAT_MEDIUM_KEY = "DateFormatMedium";
    public static final String DATE_FORMAT_SHORT_KEY = "DateFormatShort";

    public static final String ERA_BEFORE_FULL_KEY = "EraFullBefore";
    public static final String ERA_NOW_FULL_KEY = "EraFullNow";

    public static final String ERA_BEFORE_SHORT_KEY = "EraShortBefore";
    public static final String ERA_NOW_SHORT_KEY = "EraShortNow";

    public static final String FORMAT_HOUR_24_MINUTE_KEY = "FormatHour24Minute";
    public static final String FORMAT_HOUR_24_MINUTE_SECOND_KEY = "FormatHour24MinuteSecond";
    public static final String FORMAT_MONTH_ABBREV_DAY_KEY = "FormatMonthAbbrevDay";
    public static final String FORMAT_MONTH_FULL_DAY_KEY = "FormatMonthFullDay";
    public static final String FORMAT_MONTH_FULL_WEEKDAY_DAY_KEY = "FormatMonthFullWeekdayDay";
    public static final String FORMAT_MONTH_NUM_DAY_KEY = "FormatMonthNumDay";
    public static final String FORMAT_YEAR_MONTH_ABBREV_KEY = "FormatYearMonthAbbrev";

    public static final String FORMAT_YEAR_MONTH_ABBREV_DAY_KEY = "FormatYearMonthAbbrevDay";
    public static final String FORMAT_YEAR_MONTH_FULL_KEY = "FormatYearMonthFull";
    public static final String FORMAT_YEAR_MONTH_FULL_DAY_KEY = "FormatYearMonthDayFull";
    public static final String FORMAT_YEAR_MONTH_NUM_KEY = "FormatYearMonthNum";
    public static final String FORMAT_YEAR_MONTH_NUM_DAY_KEY = "FormatYearMonthNumDay";
    public static final String FORMAT_YEAR_MONTH_WEEKDAY_DAY_KEY = "FormatYearMonthWeekdayDay";
    public static final String FORMAT_YEAR_QUARTER_FULL_KEY = "FormatYearQuarterFull";
    public static final String FORMAT_YEAR_QUARTER_SHORT_KEY = "FormatYearQuarterShort";

    public static final String ATTACHMENT_UNAVAILABLE_KEY = "AttachmentUnavailable";
    public static final String MORE_ITEMS_KEY = "MoreItems";
    public static final String NO_ITEMS_FETCHED = "NoItemsFetched";

    public static final String ACL_SERVICE_NO_PERMISSIONS_FOR_DO = "dao-impl.exception.AccessControlServiceImpl.noPermissionsForDo";
    public static final String ACL_SERVICE_NO_PERMISSIONS_FOR_CHILD = "dao-impl.exception.AccessControlServiceImpl.noPermissionsForChild";
    public static final String ACL_SERVICE_NO_PERMISSIONS_CR_NOT_ALWD = "dao-impl.exception.AccessControlServiceImpl.creationNotAllowed";
    public static final String ACL_SERVICE_WRONG_ACCESS_TOKEN = "dao-impl.exception.AccessControlServiceImpl.wrongAccessToken";
    public static final String ACL_SERVICE_WRONG_SYSTEM_ACCESS_TOKEN = "dao-impl.exception.AccessControlServiceImpl.wrongSystemAccessToken";
    public static final String ACL_SERVICE_READ_PERMISSIONS_DENIED = "dao-impl.exception.AccessControlServiceImpl.readPermissionsDenied";
    
    public static final String ADD_PLUGIN_FILES_KEY = "AddPluginFiles";
}
