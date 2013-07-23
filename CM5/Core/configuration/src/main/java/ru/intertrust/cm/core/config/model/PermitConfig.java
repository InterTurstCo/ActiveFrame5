package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;

/**
 * Указывает контекстную роль, которой разрешено выполнение действия, определённого родительским тегом. 
 * @author atsvetkov
 *
 */
public class PermitConfig {

    @Attribute(required = true)
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PermitConfig that = (PermitConfig) o;

        if (role != null ? !role.equals(that.role) : that.role != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        return result;
    }
}
