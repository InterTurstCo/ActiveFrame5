package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Конфигурация прав на создание доменных объектов.
 * @author atsvetkov
 */
public class AccessMatrixCreateConfig implements Dto {

    @ElementList(entry = "permit-group", type = PermitGroup.class, required = true, inline = true)
    private List<PermitGroup> permitGroups = new ArrayList<>();

    public List<PermitGroup> getPermitGroups() {
        return permitGroups;
    }

    public void setPermitGroups(List<PermitGroup> permitGroups) {
        this.permitGroups = permitGroups;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((permitGroups == null) ? 0 : permitGroups.hashCode());
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
        AccessMatrixCreateConfig other = (AccessMatrixCreateConfig) obj;
        if (permitGroups == null) {
            if (other.permitGroups != null) {
                return false;
            }
        } else if (!permitGroups.equals(other.permitGroups)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AccessMatrixCreateConfig [permitGroups=" + permitGroups + "]";
    }

   
}
