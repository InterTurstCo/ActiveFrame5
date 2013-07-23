package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;

/**
 * Конфигурация разрешений на выполнение заданного конфигурируемого действия.
 * @author atsvetkov
 *
 */
public class ExecuteActionConfig extends BaseOperationPermitConfig {

    /**
     * Название контекстного действия, разрешения на выполнение которого выдаются
     */
    @Attribute(required = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }        

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExecuteActionConfig that = (ExecuteActionConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        if (getPermit() != null ? !getPermit().equals(that.getPermit()) : that.getPermit() != null) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + super.hashCode();
        return result;
    }
    
}
