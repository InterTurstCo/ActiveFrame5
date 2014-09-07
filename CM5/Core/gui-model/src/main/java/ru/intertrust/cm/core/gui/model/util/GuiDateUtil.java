package ru.intertrust.cm.core.gui.model.util;

import java.util.Date;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.09.2014
 *         Time: 22:52
 */
public class GuiDateUtil {

    public static Date setEndOfDay(Date date){
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);
        return date;
    }

    public static Date setStartOfDay(Date date){
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        return date;

    }
}
