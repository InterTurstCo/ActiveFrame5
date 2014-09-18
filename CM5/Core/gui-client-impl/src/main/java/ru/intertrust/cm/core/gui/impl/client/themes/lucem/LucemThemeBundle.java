package ru.intertrust.cm.core.gui.impl.client.themes.lucem;

/**
 * @author Valdemar Kostenko
 *         Date: 21.07.14
 *         Time: 12:55
 */

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import ru.intertrust.cm.core.gui.impl.client.themes.ThemeBundle;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;

public interface LucemThemeBundle extends ThemeBundle{

    @Source("lucem-theme.css")
    @CssResource.NotStrict
    public CssResource mainCss();

    @Source("navigation/navigation-tree.css")
    @CssResource.NotStrict
    public CssResource navigationTreeCss();

    @CssResource.NotStrict
    @Source("splitter/splitter.css")
    SplitterStyles splitterCss();

    @Source("images/settings.png")
    public ImageResource settingsIm();

    @Override
    @Source("images/cancel.png")
    ImageResource failedIm();

    @Override
    @Source("images/confirm.png")
    ImageResource doneIm();

    @ClientBundle.Source("images/confirmDialogDefIm.png")
    ImageResource confirmDialogWindowIm();
}