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
}
