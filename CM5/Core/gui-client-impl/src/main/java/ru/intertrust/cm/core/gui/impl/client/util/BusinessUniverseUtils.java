package ru.intertrust.cm.core.gui.impl.client.util;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.02.14
 *         Time: 13:15
 */
public class BusinessUniverseUtils {

    public static int adjustWidth(int calculatedWidth, int minWidth, int maxWidth) {
        if (calculatedWidth < minWidth) {

            return minWidth;
        }
        if (calculatedWidth > maxWidth) {
            return  maxWidth;
        }
        return  calculatedWidth;
    }
}
