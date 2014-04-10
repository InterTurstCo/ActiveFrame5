package ru.intertrust.cm.core.gui.model.csv;

import ru.intertrust.cm.core.business.api.dto.SortCriterion;

/**
 * Created by User on 08.04.2014.
 */
public class JsonSortCriterion {
    private static final String DESCENDING = "desc";
    private String field;
    private String order;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public SortCriterion.Order getTransformedOrder() {

        if (DESCENDING.equalsIgnoreCase(order)) {
            return SortCriterion.Order.DESCENDING;
        }

        return SortCriterion.Order.ASCENDING;
    }
}
