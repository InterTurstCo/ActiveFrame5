package ru.intertrust.cm.core.config.gui.navigation;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.07.2015
 *         Time: 9:33
 */
public enum NavigationPanelSecondLevelDefaultState implements Dto{
    PINNED_STATE("pinned"), UNPINNED_STATE("unpinned");
    private String state;

    NavigationPanelSecondLevelDefaultState(String state) {
        this.state = state;
    }

    public static NavigationPanelSecondLevelDefaultState forState(String state){
        NavigationPanelSecondLevelDefaultState result = null;
        for (NavigationPanelSecondLevelDefaultState secondLevelDefaultState : NavigationPanelSecondLevelDefaultState.values()) {
            if(secondLevelDefaultState.state.equalsIgnoreCase(state)){
                result = secondLevelDefaultState;
                break;
            }
        }
        return result;
    }
}
