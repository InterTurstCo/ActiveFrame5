package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.12.2015
 *         Time: 11:48
 */
public class RowItemsResponse implements Dto {
    private LinkedHashMap<String, RowItem> rowItemsMap;

    public RowItemsResponse() {
    }

    public RowItemsResponse(LinkedHashMap<String, RowItem> rowItemsMap) {
        this.rowItemsMap = rowItemsMap;
    }

    public LinkedHashMap<String, RowItem> getRowItemsMap() {
        if(rowItemsMap==null){
            rowItemsMap = new LinkedHashMap<>();
        }
        return rowItemsMap;
    }
}
