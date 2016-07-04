package ru.intertrust.cm.core.gui.model.util;

/**
 * @author Sergey.Okolot
 *         Created on 23.07.2014 17:50.
 */
public class UserSettingsHelper {
    /* common */
    public static final String ASSIGN_KEY = "=";
    public static final String DELIMITER_KEY = ";";
    public static final String ARRAY_DELIMITER = ",";
    public static final String LINK_KEY = "link";
    public static final String UNKNOWN_LINK = "unknown";
    /* collection */
    public static final String SELECTED_IDS_KEY = "ids";
    /* user settings domain object */
    public static final String DO_COLLECTION_VIEW_FIELD_KEY = "collection_view";
    public static final String DO_COLLECTION_VIEWER_FIELD_KEY = "collection_viewer";
    public static final String DO_THEME_FIELD_KEY = "theme";
    public static final String DO_SPLITTER_ORIENTATION_FIELD_KEY = "splitter_orientation";
    public static final String DO_SPLITTER_POSITION_FIELD_KEY = "splitter_position";
    public static final String DO_CUSTOM_SPLITTER_POSITION_FIELD_KEY = "custom_splitter_position";
    public static final String DO_INITIAL_NAVIGATION_LINK_KEY = "nav_link";
    public static final String DO_INITIAL_APPLICATION_NAME = "application";
    public static final String DO_NAVIGATION_PANEL_SECOND_LEVEL_PINNED_KEY = "nav_panel_level2_pinned";
    /* calendar */
    public static final String CALENDAR_SELECTED_DATE = "selectedDate";

    private UserSettingsHelper() {}
}
