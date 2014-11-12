package ru.intertrust.cm.core.gui.model.csv;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 28.05.14
 *         Time: 16:15
 */
//TODO make abstarction JsonFilters
public class JsonInitialFilters {
    private List<JsonInitialFilter> jsonInitialFilters;
    private String panelState;
    public List<JsonInitialFilter> getJsonInitialFilters() {
        return jsonInitialFilters;
    }

    public void setJsonInitialFilters(List<JsonInitialFilter> jsonInitialFilters) {
        this.jsonInitialFilters = jsonInitialFilters;
    }

    public String getPanelState() {
        return panelState;
    }

    public void setPanelState(String panelState) {
        this.panelState = panelState;
    }
}
