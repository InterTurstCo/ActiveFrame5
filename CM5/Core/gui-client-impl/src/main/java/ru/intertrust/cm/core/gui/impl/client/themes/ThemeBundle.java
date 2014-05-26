package ru.intertrust.cm.core.gui.impl.client.themes;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
public interface ThemeBundle extends ClientBundle {
    CssResource css();
    ImageResource settingsIm();
}
