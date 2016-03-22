package ru.intertrust.cm.core.gui.impl.client.themes;


import com.google.gwt.resources.client.CssResource;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.07.2014
 *         Time: 22:54
 */
public interface CommonCssResource extends CssResource {
    String headerExtendedSearch();

    String columnSettingsButton();

    String infoDialogIm();

    String errorDialogIm();

    String confirmDialogIm();

    @ClassName("favoritePanelOn")
    String favoritePanelOn();

    @ClassName("formFullSizeOn")
    String formFullSizeOn();

    @ClassName("formFullSizeOff")
    String formFullSizeOff();

    @ClassName("favoritePanelOff")
    String favoritePanelOff();

    @ClassName("configurationUploader")
    String configurationUploader();

    @ClassName("filterBoxClearButtonOn")
    String filterBoxClearButtonOn();

    @ClassName("editColumn")
    String editColumn();

    @ClassName("deleteColumn")
    String deleteColumn();

    @ClassName("editButton")
    String editButton();

    @ClassName("deleteButton")
    String deleteButton();

    @ClassName("viewButton")
    String viewButton();

    @ClassName("arrowDownButton")
    String arrowDownButton();

    @ClassName("hBFilterClearButton")
    String hBFilterClearButton();

    @ClassName("magnifierButton")
    String magnifierButton();

    @ClassName("filterOpenBtn")
    String filterOpenBtn();

    @ClassName("recalculateColumnsWidthBtn")
    String recalculateColumnsWidthBtn();

    @ClassName("refreshBtn")
    String refreshBtn();

    @ClassName("addDoBtn")
    String addDoBtn();

    @ClassName("closeBtn")
    String closeBtn();

    @ClassName("rightBottomResizeCursorArea")
    String rightBottomResizeCursorArea();

    @ClassName("leftBottomResizeCursorArea")
    String leftBottomResizeCursorArea();

    String dotIm();


}
