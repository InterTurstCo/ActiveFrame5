package ru.intertrust.cm.core.business.api.dto;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;


@Root
public class ReportShceduleParameter implements ScheduleTaskParameters{
	private static final long serialVersionUID = 3634808581082816244L;

	@Attribute
	private String name;
	
	@ElementList
	private List<ShceduleTaskReportParam> parameters;
	
	@Attribute
	private String reportContextQuery;

	@Attribute
	private String addresseeQuery;

	public List<ShceduleTaskReportParam> getParameters() {
		return parameters;
	}

	public void setParameters(List<ShceduleTaskReportParam> parameters) {
		this.parameters = parameters;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getReportContextQuery() {
        return reportContextQuery;
    }

    public void setReportContextQuery(String reportContextQuery) {
        this.reportContextQuery = reportContextQuery;
    }

    public String getAddresseeQuery() {
        return addresseeQuery;
    }

    public void setAddresseeQuery(String addresseeQuery) {
        this.addresseeQuery = addresseeQuery;
    }
    
    
	
	
}
