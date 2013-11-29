package ru.intertrust.cm.core.config.model;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.dto.Dto;

@Root
public class ReportParametersData implements Dto{
    
    @ElementList
    private List<ReportParameterData> parameters;

    public List<ReportParameterData> getParameters() {
        return parameters;
    }

    public void setParameters(List<ReportParameterData> parameters) {
        this.parameters = parameters;
    }

}
