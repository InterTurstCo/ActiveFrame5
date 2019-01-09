package ru.intertrust.cm.core.business.api.dto.globalcache;

public class PingData {
    private PingRequest request;
    private PingResponse response;
    
    public PingRequest getRequest() {
        return request;
    }
    public void setRequest(PingRequest request) {
        this.request = request;
    }
    public PingResponse getResponse() {
        return response;
    }
    public void setResponse(PingResponse response) {
        this.response = response;
    }
}
