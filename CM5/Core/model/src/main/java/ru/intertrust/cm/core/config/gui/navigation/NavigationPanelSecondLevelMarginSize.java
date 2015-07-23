package ru.intertrust.cm.core.config.gui.navigation;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.07.2015
 *         Time: 9:31
 */
public enum NavigationPanelSecondLevelMarginSize implements Dto {
    DEFAULT("default"), MINIMAL("minimal");
    private String code;

    NavigationPanelSecondLevelMarginSize(String code) {
        this.code = code;
    }
    public static NavigationPanelSecondLevelMarginSize forCode(String state){
        NavigationPanelSecondLevelMarginSize result = null;
        for (NavigationPanelSecondLevelMarginSize navigationPanelSecondLevelMarginSize : NavigationPanelSecondLevelMarginSize.values()) {
            if(navigationPanelSecondLevelMarginSize.code.equalsIgnoreCase(state)){
                result = navigationPanelSecondLevelMarginSize;
                break;
            }
        }
        return result;
    }
}
