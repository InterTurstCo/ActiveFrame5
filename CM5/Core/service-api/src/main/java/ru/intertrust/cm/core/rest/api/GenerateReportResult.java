package ru.intertrust.cm.core.rest.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ru.intertrust.cm.core.business.api.dto.Id;

public class GenerateReportResult{
    private static final long serialVersionUID = -4038466874703422858L;
    
    private String templateName;
    private String fileName;
    private Id resultId;
    
    public String getTemplateName() {
        return templateName;
    }
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public Id getResultId() {
        return resultId;
    }
    public void setResultId(Id resultId) {
        this.resultId = resultId;
    }
    
    
}
