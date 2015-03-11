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
    public static final String ADD_REPORT_FILES_KEY = "AddReportFiles";

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

    public static final String USER_NAME_KEY = "UserName";
    public static final String PASSWORD_KEY = "Password";
    public static final String SIGN_ON_KEY = "SignOn";
    public static final String REMEMBER_ME_KEY = "RememberMe";
    public static final String AUTHORIZATION_ERROR_KEY = "AuthorizationError";
    public static final String AUTHORIZATION_WRONG_PSW_ERROR_KEY = "WrongPswError";
    public static final String AUTHORIZATION_CONNECTION_ERROR_KEY = "NoConnectionError";

    public static final String OBJECT_NOT_SAVED_KEY = "ObjectMustBeSaved";

    public static final String DIGITAL_SIGNATURE_KEY = "DigitalSignature";

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
}
