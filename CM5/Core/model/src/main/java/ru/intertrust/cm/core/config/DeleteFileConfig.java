package ru.intertrust.cm.core.config;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

public class DeleteFileConfig implements Serializable {

    public enum Mode {
        IMMED, NEVER, DELAYED
    }

    @Attribute(name = "mode", required = false)
    private Mode mode;

    @Attribute(name = "delay", required = false)
    private Integer delay;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return true;
        }
        DeleteFileConfig that = (DeleteFileConfig) obj;
        return this.mode == that.mode
                && (this.delay == null ? that.delay == null : this.delay.equals(that.delay));
    }

    @Override
    public int hashCode() {
        int hash = 0;
        if (mode != null) {
            hash += mode.ordinal();
        }
        hash *= 31;
        if (delay != null) {
            hash += delay.hashCode();
        }
        return hash;
    }
}
