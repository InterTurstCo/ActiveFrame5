package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

public class TransactionTrace implements Dto {

    @Attribute
    private boolean enable;

    @Attribute(required = false)
    private int minTime = 0;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getMinTime() {
        return minTime;
    }

    public void setMinTime(int minTime) {
        this.minTime = minTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TransactionTrace sqlTrace = (TransactionTrace) o;

        if (enable != sqlTrace.enable) {
            return false;
        }

        if (minTime != sqlTrace.minTime){
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (enable ? 1 : 0);
        result = 31 * result + 7 * minTime;
        return result;
    }
}
