package ru.intertrust.cm.core.gui.model.csv;

import java.util.List;

/**
 * Created by Vitaliy Orlov on 23.08.2016.
 */
public class JsonSearchQuery {

    private String targetObjectType;

    private List<String> areas;

    private List<JsonSearchQueryFilter> filters;

    public List<String> getAreas() {
        return areas;
    }

    public void setAreas(List<String> areas) {
        this.areas = areas;
    }

    public String getTargetObjectType() {
        return targetObjectType;
    }

    public void setTargetObjectType(String targetObjectType) {
        this.targetObjectType = targetObjectType;
    }

    public List<JsonSearchQueryFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<JsonSearchQueryFilter> filters) {
        this.filters = filters;
    }
}
