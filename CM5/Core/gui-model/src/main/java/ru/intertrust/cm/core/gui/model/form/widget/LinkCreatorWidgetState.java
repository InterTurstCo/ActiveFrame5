package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;

import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.10.2014
 *         Time: 14:18
 */
public abstract class LinkCreatorWidgetState<T> extends ListWidgetState {
    private Map<String, PopupTitlesHolder> typeTitleMap;

    public Map<String, PopupTitlesHolder> getTypeTitleMap() {
        return typeTitleMap;
    }

    public void setTypeTitleMap(Map<String, PopupTitlesHolder> typeTitleMap) {
        this.typeTitleMap = typeTitleMap;
    }

    public abstract T getWidgetConfig();
}
