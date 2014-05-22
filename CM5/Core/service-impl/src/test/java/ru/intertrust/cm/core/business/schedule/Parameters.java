package ru.intertrust.cm.core.business.schedule;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

@Root
public class Parameters implements ScheduleTaskParameters {
    private static final long serialVersionUID = 1L;

    @Attribute
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
