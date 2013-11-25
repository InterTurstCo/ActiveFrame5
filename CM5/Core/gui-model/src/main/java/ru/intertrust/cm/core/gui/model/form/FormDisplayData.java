package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.MarkupConfig;

import java.util.HashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 12.10.13
 *         Time: 23:22
 */
public class FormDisplayData implements Dto {
    private FormState formState;
    private MarkupConfig markup;
    private HashMap<String, String> widgetComponents;
    private boolean debug;
    private boolean editable;

    public FormDisplayData() {
    }

    public FormDisplayData(FormState formState, MarkupConfig markup, HashMap<String, String> widgetComponents,
                           boolean debug, boolean editable) {
        this.formState = formState;
        this.markup = markup;
        this.widgetComponents = widgetComponents;
        this.debug = debug;
        this.editable = editable;
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

    public HashMap<String, String> getWidgetComponents() {
        return widgetComponents;
    }

    public void setWidgetComponents(HashMap<String, String> widgetComponents) {
        this.widgetComponents = widgetComponents;
    }

    public String getWidgetComponent(String widgetId) {
        return this.widgetComponents.get(widgetId);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
