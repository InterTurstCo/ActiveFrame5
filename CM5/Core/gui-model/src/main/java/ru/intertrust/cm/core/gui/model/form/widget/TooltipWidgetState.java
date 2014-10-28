package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.07.2014
 *         Time: 21:43
 */
public abstract class TooltipWidgetState<T> extends LinkCreatorWidgetState<T> {
    protected boolean displayingAsHyperlinks;
    protected LinkedHashMap<Id, String> tooltipValues;


    public boolean isDisplayingAsHyperlinks() {
        return displayingAsHyperlinks;
    }

    public void setDisplayingAsHyperlinks(boolean displayingAsHyperlinks) {
        this.displayingAsHyperlinks = displayingAsHyperlinks;
    }

    public LinkedHashMap<Id, String> getTooltipValues() {
        return tooltipValues;
    }

    public void setTooltipValues(LinkedHashMap<Id, String> tooltipValues) {
        this.tooltipValues = tooltipValues;
    }
    public void evictTooltipItems(){
        tooltipValues = null;

    }
    public abstract Set<Id> getSelectedIds();

}
