package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.dto.Dto;

@Root(name="parameter")
public class ReportParameterData implements Dto{
    
    public ReportParameterData(){        
    }

    public ReportParameterData(String name, String value){
        this.name = name;
        this.value = value;
    }
    
    @Attribute
    private String name;
    @Attribute
    private String value;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
