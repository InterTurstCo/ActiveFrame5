package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.dom.client.Style;

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

     public static void clearGwtStyle(Style style, String background) {
         /*style.clearLeft();
         style.setRight(20, Style.Unit.PX);
         style.clearTop();
         style.clearBottom();
         style.clearPosition();
         style.clearDisplay();*/
          style.setBackgroundColor(background);

     }
}
