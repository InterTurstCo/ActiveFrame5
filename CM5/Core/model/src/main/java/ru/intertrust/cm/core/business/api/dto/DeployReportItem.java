package ru.intertrust.cm.core.business.api.dto;

/**
 * Класс описывает один элемент устанавливаемого пакета отчета
 * @author larin
 *
 */
public class DeployReportItem implements Dto{
    private String name;
    private byte[] body;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public byte[] getBody() {
        return body;
    }
    public void setBody(byte[] body) {
        this.body = body;
    }
}
