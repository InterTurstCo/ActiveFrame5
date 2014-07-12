package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.07.2014
 *         Time: 21:43
 */
public abstract class TooltipWidgetState<T> extends ListWidgetState {
    protected boolean displayingAsHyperlinks;
    public abstract T getWidgetConfig();

    public boolean isDisplayingAsHyperlinks() {
        return displayingAsHyperlinks;
    }

    public void setDisplayingAsHyperlinks(boolean displayingAsHyperlinks) {
        this.displayingAsHyperlinks = displayingAsHyperlinks;
    }
    public abstract Set<Id> getSelectedIds();
}
