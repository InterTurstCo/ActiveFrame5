package ru.intertrust.performance.gwtrpcproxy;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;

public class GwtInteraction {
    
    @Element(name="request")
    private GwtRequest request;
    @Element(name="responce")
    private GwtResponce responce;

    public GwtRequest getRequest() {
        return request;
    }
    public void setRequest(GwtRequest request) {
        this.request = request;
    }
    public GwtResponce getResponce() {
        return responce;
    }
    public void setResponce(GwtResponce responce) {
        this.responce = responce;
    }
}
