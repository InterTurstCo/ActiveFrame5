package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.action.ActionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 18.04.2016
 * Time: 12:25
 * To change this template use File | Settings | File and Code Templates.
 */
public class TableViewerData implements Dto {
    private List<ActionContext> availableActions;

    public List<ActionContext> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<ActionContext> availableActions) {
        this.availableActions = availableActions;
    }

    public TableViewerData(){
        availableActions = new ArrayList<>();
    }

    @Override
    public String toString(){
        return "Available actions: "+availableActions.size();
    }
}
