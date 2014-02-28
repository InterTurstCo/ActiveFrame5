package ru.intertrust.cm.core.gui.impl.client.util;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 17.02.14
 *         Time: 17:14
 */
public class StringUtil {
   private StringUtil() {}; // un-instantiable

    public static String join(List<?> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        if (list == null || list.isEmpty()) {
            return "";
        }
        for (Object obj : list) {
            sb.append(obj).append(delimiter);
        }
        sb.delete(sb.length() - delimiter.length(), sb.length());
        return sb.toString();
    }

}
