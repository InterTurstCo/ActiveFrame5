package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.model.GwtIncompatible;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.11.13
 *         Time: 14:05
 */
public class GuiUtil {

    /**
     * Don't create instance of helper class.
     */
    private GuiUtil() {
    }

    @GwtIncompatible
    public static String humanReadableByteCount(Long bytes) {
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
