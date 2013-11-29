package ru.intertrust.cm.core.business.api.dto;

/**
 * Класс результата генерации отчета
 * @author larin
 *
 */
public class ReportResult {
    private String templateName;
    private String fileName;
    private byte[] report;
    
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
    public byte[] getReport() {
        return report;
    }
    public void setReport(byte[] report) {
        this.report = report;
    }
}
