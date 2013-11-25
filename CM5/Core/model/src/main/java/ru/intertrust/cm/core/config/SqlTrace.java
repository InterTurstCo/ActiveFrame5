package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

public class SqlTrace {

    @Attribute
    private boolean enable;

    @Attribute
    private boolean resolveParameters;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isResolveParameters() {
        return resolveParameters;
    }

    public void setResolveParameters(boolean resolveParameters) {
        this.resolveParameters = resolveParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SqlTrace sqlTrace = (SqlTrace) o;

        if (enable != sqlTrace.enable) {
            return false;
        }
        if (resolveParameters != sqlTrace.resolveParameters) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (enable ? 1 : 0);
        result = 31 * result + (resolveParameters ? 1 : 0);
        return result;
    }
}
