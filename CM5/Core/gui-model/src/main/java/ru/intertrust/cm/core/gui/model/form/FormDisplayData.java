package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.form.MarkupConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 12.10.13
 *         Time: 23:22
 */
public class FormDisplayData implements Dto {
    private FormState formState;
    private MarkupConfig markup;
    private boolean debug;
    private boolean editable = true;

    public FormDisplayData() {
    }

    public FormDisplayData(FormState formState, MarkupConfig markup) {
        this.formState = formState;
        this.markup = markup;
    }

    public FormDisplayData(FormState formState, MarkupConfig markup, boolean debug) {
        this(formState, markup);
        this.debug = debug;
    }

    public FormDisplayData(FormState formState, MarkupConfig markup, boolean debug, boolean editable) {
        this(formState, markup, debug);
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
