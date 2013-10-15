package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Element;

public class GlobalSettingsConfig {
    /**
     * 
     */
    private static final long serialVersionUID = -8166587368979922484L;
    public static final String NAME = "global-settings";

    @Element(name = "audit-log ", required = true)
    private AuditLog auditLog;

    public AuditLog getAuditLog() {
        return auditLog;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GlobalSettingsConfig that = (GlobalSettingsConfig)o; 
        
        if (auditLog != null ? !auditLog.equals(that.auditLog) : that.auditLog != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + ( auditLog != null ? auditLog.hashCode() : 0 );
        return result;
    }    
}
