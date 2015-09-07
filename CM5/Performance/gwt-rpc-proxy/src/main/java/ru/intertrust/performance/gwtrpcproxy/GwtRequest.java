package ru.intertrust.performance.gwtrpcproxy;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Text;

public class GwtRequest {
    @Attribute(name="method")
    private String method;
    @Attribute(name="url")
    private String url;
    @Element(required=false, data=true)
    private String body;
    @Attribute(name="content-type", required=false)
    private String contentType;
    @Element(name="file", required=false)
    private FileInfo file;
    @Element(name="json", required=false)
    private String json;
    
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public FileInfo getFile() {
        return file;
    }
    public void setFile(FileInfo file) {
        this.file = file;
    }
    public String getJson() {
        return json;
    }
    public void setJson(String json) {
        this.json = json;
    } 
    
}
