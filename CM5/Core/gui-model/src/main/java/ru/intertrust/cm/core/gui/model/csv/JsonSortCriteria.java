package ru.intertrust.cm.core.gui.model.csv;

import java.util.List;

/**
 * Created by User on 08.04.2014.
 */
public class JsonSortCriteria {
    private List<JsonSortCriterion> criterions;

    public List<JsonSortCriterion> getCriterions() {
        return criterions;
    }

    public void setCriterions(List<JsonSortCriterion> criterions) {
        this.criterions = criterions;
    }
}
