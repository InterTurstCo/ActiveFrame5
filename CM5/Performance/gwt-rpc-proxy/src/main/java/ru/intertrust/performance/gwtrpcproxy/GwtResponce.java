package ru.intertrust.performance.gwtrpcproxy;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class GwtResponce {

    @Element(required = false, data = true)
    private String body;
    @Attribute(name = "status")
    private int status;
    @Attribute(name = "content-type", required = false)
    private String contentType;
    @Element(name = "json", required = false)
    private String json;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

}
