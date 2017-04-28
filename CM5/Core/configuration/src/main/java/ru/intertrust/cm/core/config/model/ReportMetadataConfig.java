package ru.intertrust.cm.core.config.model;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.dto.Dto;

@Root
public class ReportMetadataConfig implements Dto{
    @Attribute
    private String name;
    @Attribute
    private String description;
    @Attribute
    private String mainTemplate;
    @Attribute(required=false)
    private String dataSourceClass;
    @Attribute(required=false)
    private String form;
    @Attribute(required=false)
    private Integer keepDays;
    @ElementList(required=false)
    private List<ReportParameter> parameters;
    @ElementList(name="formats", required=true)
    private List<String> formats;
    @ElementList(name="postProcessors", required=false)
    private List<String> postProcessors;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getMainTemplate() {
        return mainTemplate;
    }
    public void setMainTemplate(String mainTemplate) {
        this.mainTemplate = mainTemplate;
    }
    public String getForm() {
        return form;
    }
    public void setForm(String form) {
        this.form = form;
    }
    public String getDataSourceClass() {
        return dataSourceClass;
    }
    public void setDataSourceClass(String dataSourceClass) {
        this.dataSourceClass = dataSourceClass;
    }
    public List<ReportParameter> getParameters() {
        return parameters;
    }
    public void setParameters(List<ReportParameter> parameters) {
        this.parameters = parameters;
    }
    public List<String> getFormats() {
        return formats;
    }
    public void setFormats(List<String> formats) {
        this.formats = formats;
    }
    public Integer getKeepDays() {
        return keepDays;
    }
    public void setKeepDays(Integer keepDays) {
        this.keepDays = keepDays;
    }
    public List<String> getPostProcessors() {
        return postProcessors;
    }
    public void setPostProcessors(List<String> postProcessors) {
        this.postProcessors = postProcessors;
    }

}
