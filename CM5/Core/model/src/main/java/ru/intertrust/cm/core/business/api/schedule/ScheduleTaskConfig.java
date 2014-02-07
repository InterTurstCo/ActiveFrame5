package ru.intertrust.cm.core.business.api.schedule;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.converter.ScheduleTaskParametersConverter;

@Root
public class ScheduleTaskConfig implements Dto {

    @Element
    @Convert(ScheduleTaskParametersConverter.class)
    private ScheduleTaskParameters parameters;

    public ScheduleTaskParameters getParameters() {
        return parameters;
    }

    public void setParameters(ScheduleTaskParameters parameters) {
        this.parameters = parameters;
    }
    
}
