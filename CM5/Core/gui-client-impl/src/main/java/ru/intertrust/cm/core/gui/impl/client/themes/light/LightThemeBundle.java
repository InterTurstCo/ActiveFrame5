package ru.intertrust.cm.core.gui.impl.client.themes.light;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import ru.intertrust.cm.core.gui.impl.client.themes.def.DefaultThemeBundle;

/**
 * @author user
 *         Date: 30.05.14
 *         Time: 18:22
 */

public interface LightThemeBundle extends DefaultThemeBundle {
    @Source("light-theme.css")
    @CssResource.NotStrict
    public abstract CssResource mainCss();
    @Source("images/settings.png")
    public abstract ImageResource settingsIm();

}
