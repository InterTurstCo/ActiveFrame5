package ru.intertrust.cm.core.gui.model.util;

import com.google.gwt.core.client.GWT;
import ru.intertrust.cm.core.model.FatalException;

import java.util.Date;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.09.2014
 *         Time: 22:52
 */
public class GuiDateUtil {

    public static Date setEndOfDay(Date date) {
        if (!GWT.isClient()) {
            throw new FatalException("Method should not be used on server");
        }
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);
        return date;
    }

    public static Date setStartOfDay(Date date){
        if (!GWT.isClient()) {
            throw new FatalException("Method should not be used on server");
        }
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        return date;
    }
}
