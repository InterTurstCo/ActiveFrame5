package ru.intertrust.performance.gwtrpcproxy;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public class GwtInteractionGroup {
    @Attribute
    private String name;
    
    @ElementList(inline=true, entry="pair")
    private List<GwtInteraction> requestResponceList;

    public List<GwtInteraction> getRequestResponceList() {
        return requestResponceList;
    }

    public void setRequestResponceList(List<GwtInteraction> requestResponceList) {
        this.requestResponceList = requestResponceList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
