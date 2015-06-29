package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 24.06.2015
 *         Time: 9:17
 */
public class ExpandedRowState implements Dto {
    private int limit = 5;
    private int offset = 0;
    private Map<String, List<String>> filters;
    private List<CollectionRowItem> items = new ArrayList<>();

    public void moreItems(){
        offset+=5;
    }
    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public List<CollectionRowItem> getItems() {
        return items;
    }

    public Map<String, List<String>> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, List<String>> filters) {
        this.filters = filters;
    }

}
