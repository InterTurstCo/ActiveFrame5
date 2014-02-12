package ru.intertrust.cm.core.gui.model;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by User on 11.02.14.
 */
public class SortedMarker implements Dto {
    private boolean ascending = true;

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
}
