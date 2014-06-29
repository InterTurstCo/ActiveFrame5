package ru.intertrust.cm.core.gui.impl.client.form.widget.datebox;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.06.2014
 *         Time: 0:14
 */
public class TimeUtil {
    public static boolean showSeconds(String pattern){
        return pattern.contains("ss") || pattern.contains("SS");
    }
}
