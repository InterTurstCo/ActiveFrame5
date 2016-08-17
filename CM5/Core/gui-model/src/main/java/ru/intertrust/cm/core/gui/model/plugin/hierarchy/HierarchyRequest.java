package ru.intertrust.cm.core.gui.model.plugin.hierarchy;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.SortCriterion;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 29.07.2016
 * Time: 11:27
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyRequest implements Dto {
    private String collectionName;
    private String viewName;
    private int offset = 0;
    private int count = 0;
    private SortOrder sortOrder;
    private HierarchyCollectionConfig collectionConfig;

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public SortOrder getSortOrder() {
        if(sortOrder == null){
            sortOrder = new SortOrder();
            sortOrder.add(new SortCriterion("Id", SortCriterion.Order.ASCENDING));
        }
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public HierarchyCollectionConfig getCollectionConfig() {
        return collectionConfig;
    }

    public void setCollectionConfig(HierarchyCollectionConfig collectionConfig) {
        this.collectionConfig = collectionConfig;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}
