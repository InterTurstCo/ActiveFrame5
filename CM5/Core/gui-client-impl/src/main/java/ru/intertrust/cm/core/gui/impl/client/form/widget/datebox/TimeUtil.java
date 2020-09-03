package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.06.2014
 *         Time: 0:14
 */
public class TimeUtil {

    public static final int MILLIS_IN_SEC = 1000;
    public static final int SECONDS_IN_MINUTE = 60;
    public static final int MINUTES_IN_HOUR = 60;

    private TimeUtil() {
    }

    public static boolean showSeconds(String pattern){
        return pattern.contains("ss") || pattern.contains("SS");
    }

}
