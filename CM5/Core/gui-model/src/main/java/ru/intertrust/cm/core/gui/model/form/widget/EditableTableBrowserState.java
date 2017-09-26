package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.EditableTableBrowserConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by Ravil on 26.09.2017.
 */
public class EditableTableBrowserState extends TooltipWidgetState<EditableTableBrowserConfig> {
    private LinkedHashMap<String, String> domainFieldOnColumnNameMap;
    private EditableTableBrowserConfig editableTableBrowserConfig;
    private DomainObject rootObject;
    private String text;

    @Override
    public EditableTableBrowserConfig getWidgetConfig() {
        return editableTableBrowserConfig;
    }

    @Override
    public ArrayList<Id> getIds() {
        return null;
    }

    @Override
    public Set<Id> getSelectedIds() {
        return null;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public EditableTableBrowserConfig getEditableTableBrowserConfig() {
        return editableTableBrowserConfig;
    }

    public void setEditableTableBrowserConfig(EditableTableBrowserConfig editableTableBrowserConfig) {
        this.editableTableBrowserConfig = editableTableBrowserConfig;
    }
}
