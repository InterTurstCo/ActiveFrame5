package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Конфигурация разрешения на выполнение любой операции (read, write, delete, create-child, execute-action)
 * @author atsvetkov
 *
 */
public abstract class BaseOperationPermitConfig implements Dto {


    @ElementListUnion({
            @ElementList(entry = "permit-role", type = PermitRole.class, inline = true, required = false),
            @ElementList(entry = "permit-group", type = PermitGroup.class, inline = true, required = false),
    })
    private List<BasePermit> permitConfigs = new ArrayList<>();


    public List<BasePermit> getPermitConfigs() {
        return permitConfigs;
    }

    public void setPermitConfigs(List<BasePermit> permitConfigs) {
        this.permitConfigs = permitConfigs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((permitConfigs == null) ? 0 : permitConfigs.hashCode());
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
        BaseOperationPermitConfig other = (BaseOperationPermitConfig) obj;
        if (permitConfigs == null) {
            if (other.permitConfigs != null) {
                return false;
            }
        } else if (!permitConfigs.equals(other.permitConfigs))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BaseOperationPermitConfig [permitConfigs=" + permitConfigs + "]";
    }
    
    

}
