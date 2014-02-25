package ru.intertrust.cm.test.schedule;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;

@Root
public class TestScheduleParameters implements ScheduleTaskParameters{
    
    private static final long serialVersionUID = 1L;

    @Attribute(required=false)
    private String result;

    @Attribute(required=false)
    private boolean error;
    
    @Attribute(required=false)
    private int workTime;

    @Attribute(required=false)
    private boolean throwInterruptedOnTimeout;
    
    @Attribute(required=false)
    private boolean stopWorkOnTimeout;
    
    
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public int getWorkTime() {
        return workTime;
    }

    public void setWorkTime(int workTime) {
        this.workTime = workTime;
    }

    public boolean isThrowInterruptedOnTimeout() {
        return throwInterruptedOnTimeout;
    }

    public void setThrowInterruptedOnTimeout(boolean throwInterruptedOnTimeout) {
        this.throwInterruptedOnTimeout = throwInterruptedOnTimeout;
    }

    public boolean isStopWorkOnTimeout() {
        return stopWorkOnTimeout;
    }

    public void setStopWorkOnTimeout(boolean stopWorkOnTimeout) {
        this.stopWorkOnTimeout = stopWorkOnTimeout;
    }

    @Override
    public String toString() {
        return "TestScheduleParameters [result=" + result + ", error=" + error + ", workTime=" + workTime
                + ", throwInterruptedOnTimeout=" + throwInterruptedOnTimeout + ", stopWorkOnTimeout="
                + stopWorkOnTimeout + "]";
    }

    
}
