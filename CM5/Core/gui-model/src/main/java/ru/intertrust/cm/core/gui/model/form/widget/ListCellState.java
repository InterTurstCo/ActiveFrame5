package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ravil on 18.05.2017.
 */
public class ListCellState extends WidgetState implements Dto {
    private List<CollectionRowItem> items;
    private String headerValue;
    private Boolean counterRequired;

    public List<CollectionRowItem> getItems() {
        return items;
    }

    public void setItems(List<CollectionRowItem> items) {
        this.items = items;
    }

    public ListCellState(){
        items = new ArrayList<>();
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

    public Boolean getCounterRequired() {
        return counterRequired;
    }

    public void setCounterRequired(Boolean counterRequired) {
        this.counterRequired = counterRequired;
    }
}
