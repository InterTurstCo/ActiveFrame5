package ru.intertrust.cm.core.gui.impl.client.themes;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import ru.intertrust.cm.core.gui.impl.client.themes.def.splitter.SplitterStyles;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
public interface ThemeBundle extends ClientBundle {

    public CssResource mainCss();

    public CssResource navigationTreeCss();

    SplitterStyles splitterCss();

    ImageResource settingsIm();

    ImageResource doneIm();

    ImageResource failedIm();

    ImageResource restartRequiredIm();

    @Source("images/ext-search.png")
    ImageResource extendedSearchIm();

    @Source("images/columnSettingsIm.png")
    ImageResource columnSettingsIm();

    @ClientBundle.Source("images/confirmDialogDefIm.png")
    ImageResource confirmDialogIm();

    @ClientBundle.Source("images/errorDialogDefIm.png")
    ImageResource errorDialogIm();

    @ClientBundle.Source("images/infoDialogDefIm.png")
    ImageResource infoDialogIm();

    @Source("common.css")
    public CommonCssResource commonCss();
}
