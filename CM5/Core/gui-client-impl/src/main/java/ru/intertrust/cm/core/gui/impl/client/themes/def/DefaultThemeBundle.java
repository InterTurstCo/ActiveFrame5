package ru.intertrust.cm.core.gui.impl.client.themes.def;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import ru.intertrust.cm.core.gui.impl.client.themes.ThemeBundle;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;

public interface DefaultThemeBundle extends ThemeBundle{

    @Source("custom-theme.css")
    @CssResource.NotStrict
    public CssResource mainCss();

    @Source("navigation/navigation-tree.css")
    @CssResource.NotStrict
    public CssResource navigationTreeCss();

    @CssResource.NotStrict
    @Source("splitter/splitter.css")
    SplitterStyles splitterCss();

    @Source("ru/intertrust/cm/core/gui/impl/images/settings.png")
    public ImageResource settingsIm();

    @Override
    @Source("ru/intertrust/cm/core/gui/impl/images/cancel.png")
    ImageResource failedIm();

    @Override
    @Source("ru/intertrust/cm/core/gui/impl/images/confirm.png")
    ImageResource doneIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/confirmDialogDefIm.png")
    ImageResource confirmDialogIm();

    @Source("custom-theme.css")
    @CssResource.NotStrict
    public DefThemeCssResource defCss();

}