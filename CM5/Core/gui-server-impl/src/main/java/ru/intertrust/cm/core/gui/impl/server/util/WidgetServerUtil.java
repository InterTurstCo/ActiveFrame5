package ru.intertrust.cm.core.gui.impl.server.util;

import java.util.Iterator;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 07.12.2014
 *         Time: 17:40
 */
public class WidgetServerUtil {

    public static void doLimit(Iterable iterable, int limit){
        boolean limited = limit > -1;
        if(limited){
            Iterator iterator = iterable.iterator();
            int count = 0;
            while (iterator.hasNext()){
                count++;
                iterator.next();
                if(count > limit){
                    iterator.remove();
                }

            }
        }
    }
}
