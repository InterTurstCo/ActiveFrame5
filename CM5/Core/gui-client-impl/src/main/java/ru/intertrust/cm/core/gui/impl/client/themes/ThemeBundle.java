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

    @Source("ru/intertrust/cm/core/gui/impl/images/settings.png")
    ImageResource settingsIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/confirm.png")
    ImageResource doneIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/cancel.png")
    ImageResource failedIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/ext-search.png")
    ImageResource extendedSearchIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/columnSettingsIm.png")
    ImageResource columnSettingsIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/confirmDialogDefIm.png")
    ImageResource confirmDialogIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/errorDialogDefIm.png")
    ImageResource errorDialogIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/infoDialogDefIm.png")
    ImageResource infoDialogIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/form-fullsize-on.png")
    ImageResource formFullSizeOnActionIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/form-fullsize-off.png")
    ImageResource formFullSizeOffActionIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/favorite-panel-on.png")
    ImageResource favoritePanelOnActionIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/icon-upload.png")
    ImageResource configurationUploaderIcon();

    @Source("ru/intertrust/cm/core/gui/impl/images/favorite-panel-off.png")
    ImageResource favoritePanelOffActionIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/filterClearButton.png")
    ImageResource filterClearButtonIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/arrowDown.png")
    ImageResource arrowDownIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/magnifier.png")
    ImageResource magnifierIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/filterOpenBtn.png")
    ImageResource filterOpenBtnIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/recalculateColumnsWidthIm.png")
    ImageResource recalculateColumnsWidthBtnIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/addButton.png")
    ImageResource addDoBtnIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/refreshBtn.gif")
    ImageResource refreshBtnIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/icon-close.png")
    ImageResource closeIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/icon-edit.png")
    ImageResource editIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/icon-delete.png")
    ImageResource deleteIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/icon-edit.png")
    ImageResource viewIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/rightBottomResizeArea.jpg")
    ImageResource rightBottomResizeAreaIm();

    @Source("ru/intertrust/cm/core/gui/impl/images/leftBottomResizeArea.jpg")
    ImageResource leftBottomResizeAreaIm();

    @Source("common.css")
    public CommonCssResource commonCss();
}
