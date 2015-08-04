package ru.intertrust.cm.core.config.gui.navigation;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.08.2015
 *         Time: 19:24
 */
public enum RowsSelectionDefaultState implements Dto {
    SELECTED("selected"), EMPTY("empty");
    private String code;

    RowsSelectionDefaultState(String code) {
        this.code = code;
    }

    public static RowsSelectionDefaultState forCode(String code) {
        RowsSelectionDefaultState result = null;
        for (RowsSelectionDefaultState rowsSelectionDefaultState : RowsSelectionDefaultState.values()) {
            if (rowsSelectionDefaultState.code.equalsIgnoreCase(code)) {
                result = rowsSelectionDefaultState;
                break;
            }
        }
        return result;
    }
}