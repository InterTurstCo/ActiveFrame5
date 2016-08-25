package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import ru.intertrust.cm.core.config.search.ExtendedSearchPopupConfig;

/**
 * Created by Vitaliy Orlov on 05.08.2016.
 */
public class ExtendedSearchDialogHelper {

    public static final int DEFAULT_WIDTH = 650;
    public static final int DEFAULT_HEIGHT = 600;
    public static final int MINIMAL_HEIGHT = 270;
    public static final int MINIMAL_WIDTH= 300;

    public static int getHeight(ExtendedSearchPopupConfig popupConfig){
        if(popupConfig == null || popupConfig.getDialogWindowConfig() == null){
            return DEFAULT_HEIGHT;
        } else {
            return popupConfig.getDialogWindowConfig().getHeight() == null ? DEFAULT_HEIGHT : parseStringSize(popupConfig.getDialogWindowConfig().getHeight());
        }
    }

    public static String getHeightInPixel(ExtendedSearchPopupConfig popupConfig){
        return getHeight(popupConfig)+ "px";
    }

    public static String getWidthInPixel(ExtendedSearchPopupConfig popupConfig){
        return getWidth(popupConfig)+ "px";
    }

    public static int getWidth(ExtendedSearchPopupConfig popupConfig){
        if(popupConfig == null || popupConfig.getDialogWindowConfig() == null){
            return DEFAULT_WIDTH;
        } else {
            return popupConfig.getDialogWindowConfig().getWidth() == null ? DEFAULT_WIDTH
                    : parseStringSize(popupConfig.getDialogWindowConfig().getWidth());
        }
    }

    public static boolean isResizable (ExtendedSearchPopupConfig popupConfig){
        if(popupConfig == null || popupConfig.getDialogWindowConfig() == null){
            return false;
        } else {
            return popupConfig.getDialogWindowConfig().isResizable();
        }
    }

    public static int parseStringSize(String size){
        if(size != null && !size.isEmpty()){
            if(size.indexOf("px") != -1){
                return parseStringSize(size.substring(0, size.indexOf("px")));
            }else{
                return Integer.valueOf(size.trim());
            }
        }
        return 0;
    }
}
