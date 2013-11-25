package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

public class AuditLog {

    @Attribute
    private boolean enable;

    public boolean isEnable() {
        return enable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuditLog that = (AuditLog)o;

        if (enable != that.enable) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + ( enable ? 1 : 0 );
        return result;
    }
}
