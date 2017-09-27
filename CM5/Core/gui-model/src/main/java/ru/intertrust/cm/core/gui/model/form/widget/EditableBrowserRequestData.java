package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.EditableTableBrowserConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * Created by Ravil on 27.09.2017.
 */
public class EditableBrowserRequestData implements Dto {
    private EditableTableBrowserConfig config;
    private String defaultValue;
    private FormState formState;

    public EditableTableBrowserConfig getConfig() {
        return config;
    }

    public void setConfig(EditableTableBrowserConfig config) {
        this.config = config;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public FormState getFormState() {
        return formState;
    }

    public void setFormState(FormState formState) {
        this.formState = formState;
    }
}
