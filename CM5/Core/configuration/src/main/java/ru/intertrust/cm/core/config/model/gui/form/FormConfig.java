package ru.intertrust.cm.core.config.model.gui.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Denis Mitavskiy
 *         Date: 05.09.13
 *         Time: 15:58
 */
public class FormConfig implements Dto {
    private MarkupConfig markup;

    public MarkupConfig getMarkup() {
        return markup;
    }

    public void setMarkup(MarkupConfig markup) {
        this.markup = markup;
    }
}
