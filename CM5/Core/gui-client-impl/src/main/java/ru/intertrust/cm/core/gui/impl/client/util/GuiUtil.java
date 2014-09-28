package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.09.2014
 *         Time: 10:41
 */
public class GuiUtil {
    public static boolean isChildClicked(ClickEvent event, String id) {
        NodeList<Element> checkBoxes = Document.get().getElementById(id).getElementsByTagName("input");
        Element target = Element.as(event.getNativeEvent().getEventTarget());
        for (int i = 0; i < checkBoxes.getLength(); i++) {
            if (checkBoxes.getItem(i).isOrHasChild(target)) {
                return true;
            }
        }
        NodeList<Element> links = Document.get().getElementById(id).getElementsByTagName("div");
        for (int i = 0; i < links.getLength(); i++) {
            if (links.getItem(i).isOrHasChild(target)) {
                return true;
            }
        }
        return false;
    }
}
