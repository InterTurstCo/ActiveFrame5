package ru.intertrust.cm.core.gui.impl.client.themes.def;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import ru.intertrust.cm.core.gui.impl.client.themes.ThemeBundle;

public interface DefaultThemeBundle extends ThemeBundle{

    @Source("custom-theme.css")
    @CssResource.NotStrict
    public CssResource css();

    @Source("images/settings.png")
    public ImageResource settingsIm();


}