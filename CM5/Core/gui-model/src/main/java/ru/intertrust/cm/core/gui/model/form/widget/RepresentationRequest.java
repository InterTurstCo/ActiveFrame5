package ru.intertrust.cm.core.gui.model.form.widget;

import java.util.Collection;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class RepresentationRequest implements Dto {
    private Collection<Id> ids;
    private String pattern;
    private FormattingConfig formattingConfig;
    private SummaryTableConfig summaryTableConfig;
    public RepresentationRequest() {
    }

    public RepresentationRequest(Collection<Id> ids, String pattern, FormattingConfig formattingConfig) {
        this.ids = ids;
        this.pattern = pattern;
        this.formattingConfig = formattingConfig;
    }

    public RepresentationRequest(Collection<Id> ids, String pattern, SummaryTableConfig summaryTableConfig) {
        this.ids = ids;
        this.pattern = pattern;
        this.summaryTableConfig = summaryTableConfig;
    }

    public Collection<Id> getIds() {
        return ids;
    }

    public void setIds(Collection<Id> ids) {
        this.ids = ids;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
    }

    public SummaryTableConfig getSummaryTableConfig() {
        return summaryTableConfig;
    }

    public void setSummaryTableConfig(SummaryTableConfig summaryTableConfig) {
        this.summaryTableConfig = summaryTableConfig;
    }
}
