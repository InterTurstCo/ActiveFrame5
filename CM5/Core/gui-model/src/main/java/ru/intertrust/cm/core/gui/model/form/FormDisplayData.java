package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.ScriptFileConfig;

import java.util.HashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 12.10.13
 *         Time: 23:22
 */
public class FormDisplayData implements Dto {
    private FormState formState;
    private MarkupConfig markup;
    private ToolBarConfig toolBarConfig;
    private ScriptFileConfig scriptFileConfig;
    private boolean debug;
    private String minWidth;
    private DomainObject status;

    public FormDisplayData() {
    }

    public FormDisplayData(FormState formState, MarkupConfig markup, HashMap<String, String> widgetComponents,
                           String minWidth, boolean debug) {
        this.minWidth = minWidth;
        this.formState = formState;
        this.markup = markup;
        this.debug = debug;
    }

    public FormState getFormState() {
        return formState;
    }

    public void setFormState(FormState formState) {
        this.formState = formState;
    }

    /**
     * Возвращает разметку формы.
     * @return разметку формы
     */
    public MarkupConfig getMarkup() {
        return markup;
    }

    /**
     * Устанавливает разметку формы.
     * @param markup разметка формы
     */
    public void setMarkup(MarkupConfig markup) {
        this.markup = markup;
    }

    public String getWidgetComponent(String widgetId) {
        return getFormState().getWidgetComponent(widgetId);
    }

    public String getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(String minWidth) {
        this.minWidth = minWidth;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public ToolBarConfig getToolBarConfig() {
        return toolBarConfig;
    }

    public void setToolBarConfig(ToolBarConfig toolBarConfig) {
        this.toolBarConfig = toolBarConfig;
    }

    public ScriptFileConfig getScriptFileConfig() {
        return scriptFileConfig;
    }

    public void setScriptFileConfig(ScriptFileConfig scriptFileConfig) {
        this.scriptFileConfig = scriptFileConfig;
    }

    public DomainObject getStatus() {
        return status;
    }

    public void setStatus(DomainObject status) {
        this.status = status;
    }
}
