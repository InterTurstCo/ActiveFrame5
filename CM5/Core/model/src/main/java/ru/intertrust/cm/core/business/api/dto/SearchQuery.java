package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Класс для хранения и передачи запросов на расширенный поиск
 * 
 * @author apirozhkov
*/
public class SearchQuery implements Dto {

    private ArrayList<String> areas = new ArrayList<>();
    private String targetObjectType;
    private HashMap<String, SearchFilter> filters = new HashMap<>();

    public List<String> getAreas() {
        return areas;
    }

    public void addArea(String area) {
        areas.add(area);
    }

    public void addAreas(Collection<String> areas) {
        this.areas.addAll(areas);
    }

    public void removeArea(String area) {
        areas.remove(area);
    }

    public void clearAreas() {
        areas = new ArrayList<>();
    }

    public String getTargetObjectType() {
        return targetObjectType;
    }

    public void setTargetObjectType(String targetObjectType) {
        this.targetObjectType = targetObjectType;
    }

    public Collection<SearchFilter> getFilters() {
        return filters.values();
    }

    public void addFilter(SearchFilter filter) {
        filters.put(filter.getFieldName(), filter);
    }

    public void addFilters(Collection<SearchFilter> filters) {
        for (SearchFilter filter : filters) {
            addFilter(filter);
        }
    }

    public void removeFilter(SearchFilter filter) {
        filters.remove(filter.getFieldName());
    }

    public void clearFilters() {
        filters = new HashMap<>();
    }
}
