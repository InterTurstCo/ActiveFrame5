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

    @ClientBundle.Source("images/form-fullsize-on.png")
    ImageResource formFullSizeOnActionIm();

    @ClientBundle.Source("images/form-fullsize-off.png")
    ImageResource formFullSizeOffActionIm();

    @ClientBundle.Source("images/favorite-panel-on.png")
    ImageResource favoritePanelOnActionIm();

    @ClientBundle.Source("images/icon-upload.png")
    ImageResource configurationUploaderIcon();

    @ClientBundle.Source("images/favorite-panel-off.png")
    ImageResource favoritePanelOffActionIm();

    @ClientBundle.Source("images/filterClearButton.png")
    ImageResource filterClearButtonIm();

    @ClientBundle.Source("images/arrowDown.png")
    ImageResource arrowDownIm();

    @ClientBundle.Source("images/magnifier.png")
    ImageResource magnifierIm();

    @ClientBundle.Source("images/filterOpenBtn.png")
    ImageResource filterOpenBtnIm();

    @ClientBundle.Source("images/recalculateColumnsWidthIm.png")
    ImageResource recalculateColumnsWidthBtnIm();

    @Source("common.css")
    public CommonCssResource commonCss();
}
