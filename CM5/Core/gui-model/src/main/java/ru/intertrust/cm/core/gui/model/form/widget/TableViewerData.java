package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 18.04.2016
 * Time: 12:25
 * To change this template use File | Settings | File and Code Templates.
 */
public class TableViewerData implements Dto {
    private List<ActionContext> availableActions;

    private Map<String,Map> idsActions;

    private List<Id> selectedIds;

    public List<ActionContext> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<ActionContext> availableActions) {
        this.availableActions = availableActions;
    }

    public TableViewerData(){
        availableActions = new ArrayList<>();
        idsActions = new HashMap<>();
    }

    @Override
    public String toString(){
        return "Available actions: "+availableActions.size()+" available multiple actions: "+idsActions.size();
    }

    public Map<String, Map> getIdsActions() {
        return idsActions;
    }

    public void setIdsActions(Map<String, Map> idsActions) {
        this.idsActions = idsActions;
    }

    public List<Id> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(List<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }
}
