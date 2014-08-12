package ru.intertrust.cm.core.gui.impl.client.themes.light;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import ru.intertrust.cm.core.gui.impl.client.themes.def.DefaultThemeBundle;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.05.14
 *         Time: 18:22
 */

public interface LightThemeBundle extends DefaultThemeBundle {
    @Source("light-theme.css")

    @CssResource.NotStrict
    public CssResource mainCss();

    @CssResource.NotStrict
    @Source("splitter/splitter.css")
    SplitterStyles splitterCss();

    @Source("images/settings.png")
    public ImageResource settingsIm();

    @ClientBundle.Source("images/confirmDialogDefIm.png")
    ImageResource confirmDialogWindowIm();

}
