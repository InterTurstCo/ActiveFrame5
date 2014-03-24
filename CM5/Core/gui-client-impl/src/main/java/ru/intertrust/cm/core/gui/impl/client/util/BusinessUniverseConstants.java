package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 31.01.14
 *         Time: 13:15
 */
public class BusinessUniverseConstants {
    public static final String CHECK_BOX_COLUMN_NAME = "checkBox";
    public static final String SORT_ARROWS = "([↓↑])";
    public static final String DESCEND_ARROW = "↓";
    public static final String ASCEND_ARROW = "↑";
    public static final String DISPLAY_STYLE_BlOCK = "table";
    // @FIXME will be removed
    public static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");
    public static final String FOOTER_SHORT = "footer-short";
    public static final String FOOTER_LONG = "footer-long";
    public static final int COLLECTION_VIEW_SIZE_SCROLL_WIDTH = 20;
    public static final int COLLECTION_WIDGET_HEADER_ROW = 35; //не учитывает изменение высоты шапки таблицы
    public static final int COLLECTION_WIDGET_HEADER_ROW_WITH_SIMPLE_SEARCH = 70; //не учитывает изменение высоты шапки таблицы
    public static final int COLLECTION_COLUMN_SEARCH_PANEL_WIDTH = 27;
    public static final int COLLECTION_BOTTOM_SCROLL_HEIGHT = 20;
    public static final int MAX_COLUMN_WIDTH = 999999999;
    public static final int MIN_COLUMN_WIDTH = 100;
    public static final int COLLECTION_HEADER_HEIGHT = 10;
    public static final String DATE_TIME_TYPE = "datetime";
    public static final String TIMELESS_DATE_TYPE = "timelessDate";
    public static final String EMPTY_VALUE = "";
    public static final int CHECK_BOX_MIN_WIDTH = 35;
    public static final int CHECK_BOX_MAX_WIDTH = 35;
    public static final String HEADER_INPUT_ID_PART = "input";
    public static final String HEADER_CLEAR_BUTTON_ID_PART = "clear-button";
    public static final String HEADER_OPEN_DATE_PICKER_BUTTON_ID_PART = "date-picker";
}
