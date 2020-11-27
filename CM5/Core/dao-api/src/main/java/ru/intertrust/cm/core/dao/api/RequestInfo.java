package ru.intertrust.cm.core.dao.api;

/**
 * Информация о запросе данных
 */
public class RequestInfo {
    private String clientIp;
    private String clientAddress;

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }
}
