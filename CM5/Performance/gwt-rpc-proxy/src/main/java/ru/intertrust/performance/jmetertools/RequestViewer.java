package ru.intertrust.performance.jmetertools;

public class RequestViewer {
    private String service;
    private String method;
    private Object[] parameters;

    public RequestViewer(String service, String method, Object[] parameters) {
        this.service = service;
        this.method = method;
        this.parameters = parameters;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

}