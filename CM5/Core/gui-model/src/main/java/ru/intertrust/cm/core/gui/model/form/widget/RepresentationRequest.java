package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class RepresentationRequest implements Dto {
    private List<Id> ids;
    private String pattern;
    private FormattingConfig formattingConfig;
    private SummaryTableConfig summaryTableConfig;
    private FormState createdObjectState;
    public RepresentationRequest() {
    }

    public RepresentationRequest(List<Id> ids, String pattern, FormattingConfig formattingConfig) {
        this.ids = ids;
        this.pattern = pattern;
        this.formattingConfig = formattingConfig;
    }

    public RepresentationRequest(List<Id> ids, String pattern, SummaryTableConfig summaryTableConfig) {
        this.ids = ids;
        this.pattern = pattern;
        this.summaryTableConfig = summaryTableConfig;
    }

    public RepresentationRequest(FormState createdObjectState, SummaryTableConfig summaryTableConfig) {
         this.createdObjectState = createdObjectState;
         this.summaryTableConfig = summaryTableConfig;
    }

    public List<Id> getIds() {
        return ids;
    }

    public void setIds(List<Id> ids) {
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

    public FormState getCreatedObjectState() {
        return createdObjectState;
    }

    public void setCreatedObjectState(FormState createdObjectState) {
        this.createdObjectState = createdObjectState;
    }
}
