package ru.intertrust.cm.core.gui.impl.client.themes.taurika;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import ru.intertrust.cm.core.gui.impl.client.themes.ThemeBundle;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 04.10.2016
 * Time: 15:01
 * To change this template use File | Settings | File and Code Templates.
 */
public interface TaurikaThemeBundle extends ThemeBundle {
    @Source("taurika-theme.css")
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
    ImageResource confirmDialogWindowIm();

    @Source("taurika-theme.css")
    @CssResource.NotStrict
    public TaurikaThemeCssResource lucemCss();
}
