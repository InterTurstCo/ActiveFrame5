package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SuggestBoxConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 22.10.13
 * Time: 16:22
 * To change this template use File | Settings | File Templates.
 */
public class SuggestBoxState extends LinkEditingWidgetState {
    private LinkedHashMap<Id, String> listValues;
    private ArrayList<Id> selectedIds;
    private SuggestBoxConfig suggestBoxConfig;

    public LinkedHashMap<Id, String> getListValues() {
        return listValues;
    }

    public void setListValues(LinkedHashMap<Id, String> listValues) {
        this.listValues = listValues;
    }

    public ArrayList<Id> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(ArrayList<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }

    @Override
    public ArrayList<Id> getIds() {
        return selectedIds;
    }

    public SuggestBoxConfig getSuggestBoxConfig() {
        return suggestBoxConfig;
    }

    public void setSuggestBoxConfig(SuggestBoxConfig suggestBoxConfig) {
        this.suggestBoxConfig = suggestBoxConfig;
    }

}
