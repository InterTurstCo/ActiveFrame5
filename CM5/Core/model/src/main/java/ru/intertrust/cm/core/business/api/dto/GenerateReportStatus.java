package ru.intertrust.cm.core.business.api.dto;

/**
 * Класс описывает статус формирования отчета
 * @author larin
 *
 */
public class GenerateReportStatus implements Dto {
    private String message;
    private Integer percentage;
    
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Integer getPercentage() {
        return percentage;
    }
    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }
}
