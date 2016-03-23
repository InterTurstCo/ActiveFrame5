package ru.intertrust.cm.core.gui.impl.client.themes.dark;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import ru.intertrust.cm.core.gui.impl.client.themes.def.DefaultThemeBundle;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */

public interface DarkThemeBundle extends DefaultThemeBundle {
    @Source("dark-theme.css")
    @CssResource.NotStrict
    public abstract CssResource mainCss();
    @Source("ru/intertrust/cm/core/gui/impl/images//setting-dark.png")
    public abstract ImageResource settingsIm();

    @Source("dark-theme.css")
    @CssResource.NotStrict
    public DarkThemeCssResource darkCss();

}
