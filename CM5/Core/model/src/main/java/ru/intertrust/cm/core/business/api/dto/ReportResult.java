package ru.intertrust.cm.core.business.api.dto;

import com.healthmarketscience.rmiio.RemoteInputStream;

/**
 * Класс результата генерации отчета
 * @author larin
 *
 */
public class ReportResult implements Dto{
	private static final long serialVersionUID = -4038466874703422858L;
	private String templateName;
    private String fileName;
    private RemoteInputStream report;
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
    public RemoteInputStream getReport() {
        return report;
    }
    public void setReport(RemoteInputStream report) {
        this.report = report;
    }
	public Id getResultId() {
		return resultId;
	}
	public void setResultId(Id resultId) {
		this.resultId = resultId;
	}
}
