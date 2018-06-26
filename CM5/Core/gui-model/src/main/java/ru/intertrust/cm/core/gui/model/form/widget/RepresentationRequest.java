package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SummaryTableConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;

import java.util.LinkedHashMap;
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
    private String linkedFormName;
    private String collectionName;
    private String handlerName;
    private String realFormName;
    private Id rootId;
    private LinkedHashMap<String, FormState> newFormStates;

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
    public RepresentationRequest(List<Id> ids, String pattern, String collectionName,FormattingConfig formattingConfig) {
        this.ids = ids;
        this.pattern = pattern;
        this.collectionName = collectionName;
        this.formattingConfig = formattingConfig;
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

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public FormState getCreatedObjectState() {
        return createdObjectState;
    }

    public void setCreatedObjectState(FormState createdObjectState) {
        this.createdObjectState = createdObjectState;
    }

    public String getLinkedFormName() {
        return linkedFormName;
    }

    public void setLinkedFormName(String linkedFormName) {
        this.linkedFormName = linkedFormName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Id getRootId() {
        return rootId;
    }

    public void setRootId(Id rootId) {
        this.rootId = rootId;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public LinkedHashMap<String, FormState> getNewFormStates() {
        return newFormStates;
    }

    public void setNewFormStates(LinkedHashMap<String, FormState> newFormStates) {
        this.newFormStates = newFormStates;
    }

    public String getRealFormName() {
        return realFormName;
    }

    public void setRealFormName(String realFormName) {
        this.realFormName = realFormName;
    }
}
