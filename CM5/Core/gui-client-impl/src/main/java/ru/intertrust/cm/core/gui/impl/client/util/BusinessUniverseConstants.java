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
    public static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");

    public static final int COLLECTION_VIEW_SIZE_SCROLL_WIDTH = 20;
    public static final int COLLECTION_WIDGET_HEADER_ROW = 35; //не учитывает изменение высоты шапки таблицы
    public static final int COLLECTION_WIDGET_HEADER_ROW_WITH_SIMPLE_SEARCH = 70; //не учитывает изменение высоты шапки таблицы
    public static final int COLLECTION_COLUMN_SEARCH_PANEL_WIDTH = 27;
    public static final int COLLECTION_BOTTOM_SCROLL_HEIGHT = 20;
    public static final int MAX_COLUMN_WIDTH = 999999999;
}
