package ru.intertrust.performance.jmetertools;

public class RequestViewer {
    private String moduleBaseUrl;
    private String policyNameAlias;
    private String service;
    private String method;
    private Object[] parameters;
    private Class[] parameterTypes;

    public RequestViewer(String service, String method, Object[] parameters, String moduleBaseUrl, Class[] parameterTypes, String policyNameAlias) {
        this.service = service;
        this.method = method;
        this.parameters = parameters;
        this.moduleBaseUrl = moduleBaseUrl;
        this.parameterTypes = parameterTypes;
        this.policyNameAlias = policyNameAlias;
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

    public String getModuleBaseUrl() {
        return moduleBaseUrl;
    }

    public void setModuleBaseUrl(String moduleBaseUrl) {
        this.moduleBaseUrl = moduleBaseUrl;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getPolicyNameAlias() {
        return policyNameAlias;
    }

    public void setPolicyNameAlias(String policyNameAlias) {
        this.policyNameAlias = policyNameAlias;
    }
 }