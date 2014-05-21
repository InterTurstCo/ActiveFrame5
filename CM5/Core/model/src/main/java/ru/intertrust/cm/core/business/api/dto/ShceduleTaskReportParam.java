package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementUnion;

public class ShceduleTaskReportParam implements Dto{
    private static final long serialVersionUID = -4729932020532586115L;

    @Attribute
	private String name;
	
	@ElementUnion({
	      @Element(name="stringValue", type=String.class),
	      @Element(name="longValue", type=Long.class),
	      @Element(name="dateValue", type=Date.class),
	      @Element(name="relativeDateValue", type=RelativeDate.class),
	      @Element(name="queryValue", type=QueryParameter.class)
	   })
	private Object value;

	public ShceduleTaskReportParam(){
	}

    public ShceduleTaskReportParam(String name, Object value){
        this.name = name;
        this.value = value;
    }
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
