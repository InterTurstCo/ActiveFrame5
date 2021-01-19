package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Указывает контекстную роль или динамическую группу, которой разрешено выполнение действия, определённого родительским
 * тегом (read, write, delete, create-child, execute-action)
 * @author atsvetkov
 */
public class BasePermit implements Dto{

    public BasePermit(){
    }

    public BasePermit(String name){
        this.name = name;
    }

    @Attribute(required = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BasePermit other = (BasePermit) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BasePermit [name=" + name + "]";
    }
    
    
}
