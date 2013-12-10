package ru.intertrust.cm.test.schedule;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

@Root
public class TestScheduleParameters implements ScheduleTaskParameters{
    
    @Attribute
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    } 
}
