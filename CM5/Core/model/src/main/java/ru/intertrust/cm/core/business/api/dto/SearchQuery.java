package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SearchQuery implements Dto {

    private ArrayList<String> areas = new ArrayList<>();

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
}
