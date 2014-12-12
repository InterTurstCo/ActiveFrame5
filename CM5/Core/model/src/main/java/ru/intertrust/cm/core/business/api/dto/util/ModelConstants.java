package ru.intertrust.cm.core.business.api.dto.util;

import java.text.SimpleDateFormat;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 25.07.2014
 *         Time: 23:16
 */
public class ModelConstants {
    //domain object field types
    public static final String STRING_TYPE = "string";
    public static final String TEXT_TYPE = "text";
    public static final String LONG_TYPE = "long";
    public static final String DECIMAL_TYPE = "decimal";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String REFERENCE_TYPE = "reference";
    public static final String TIMELESS_DATE_TYPE = "timelessDate";
    public static final String DATE_TIME_TYPE = "datetime";
    public static final String DATE_TIME_WITH_TIME_ZONE_TYPE = "dateTimeWithTimeZone";
    public static final String PASSWORD_TYPE = "password";
    public static final String LIST_TYPE = "list";
    // calculating values for collection plugin
    public static final int INIT_ROWS_NUMBER = 55;
    public static final int ONE_ROW_SIZE = 10;

    //collection image sizes
    public static final String COLLECTION_IMAGE_WIDTH = "25px";
    public static final String COLLECTION_IMAGE_HEIGHT = "25px";
    //GUI date parsing patterns
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String TIMELESS_DATE_FORMAT = "yyyy-MM-dd";


    public static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(ModelConstants.DATE_TIME_FORMAT);
    public  static final SimpleDateFormat TIMELESS_DATE_FORMATTER = new SimpleDateFormat(ModelConstants.TIMELESS_DATE_FORMAT);
}
