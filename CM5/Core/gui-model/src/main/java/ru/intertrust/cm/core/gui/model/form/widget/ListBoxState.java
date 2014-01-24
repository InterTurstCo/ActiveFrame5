package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Denis Mitavskiy
 *         Date: 15.10.13
 *         Time: 19:48
 */
public class ListBoxState extends ValueListWidgetState {
    private ArrayList<ArrayList<Id>> selectedIds;
    private HashMap<Id, Integer> idFieldPathIndexMapping;
    private FieldPath[] fieldPaths;

    public ArrayList<ArrayList<Id>> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(ArrayList<ArrayList<Id>> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public FieldPath[] getFieldPaths() {
        return fieldPaths;
    }

    public void setFieldPaths(FieldPath[] fieldPaths) {
        this.fieldPaths = fieldPaths;
    }

    @Override
    public ArrayList<Id> getIds() {
        ArrayList<Id> result = new ArrayList<Id>();
        ArrayList<ArrayList<Id>> fieldPathsIds = getFieldPathsIds();
        if (fieldPathsIds != null) {
            for (ArrayList<Id> idsList : fieldPathsIds) {
                result.addAll(idsList);
            }
        }
        return result;
    }

    private ArrayList<ArrayList<Id>> getFieldPathsIds() {
        return selectedIds;
    }

    public void setIdFieldPathIndexMapping(HashMap<Id, Integer> idFieldPathIndexMapping) {
        this.idFieldPathIndexMapping = idFieldPathIndexMapping;
    }

    public Integer getFieldPathIndex(Id id) {
        return idFieldPathIndexMapping.get(id);
    }

    public HashSet<Id> getSelectedIdsSet() {
        final HashSet<Id> selectedIdsSet = new HashSet<Id>();
        if (selectedIds != null) {
            for (ArrayList<Id> selectedIdList : selectedIds) {
                selectedIdsSet.addAll(selectedIdList);
            }
        }
        return selectedIdsSet;
    }
}
