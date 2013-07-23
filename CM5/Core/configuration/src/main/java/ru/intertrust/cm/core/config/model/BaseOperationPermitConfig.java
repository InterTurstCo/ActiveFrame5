package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Element;

/**
 * Конфигурация разрешения на выполнение любой операции (read, write, delete, create-child, execute-action)
 * @author atsvetkov
 *
 */
public abstract class BaseOperationPermitConfig {

    @Element(name = "permit")
    private PermitConfig permit;

    public PermitConfig getPermit() {
        return permit;
    }

    public void setPermit(PermitConfig permit) {
        this.permit = permit;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseOperationPermitConfig that = (BaseOperationPermitConfig) o;

        if (permit != null ? !permit.equals(that.permit) : that.permit != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = permit != null ? permit.hashCode() : 0;
        return result;
    }


}
