package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Конфигурация матрицы доступа.
 * @author atsvetkov
 *
 */
public class AccessMatrixStatusConfig implements Dto {

    @Attribute(required = true)
    private String name;

    @ElementListUnion({
            @ElementList(entry = "read", type = ReadConfig.class, inline = true),
            @ElementList(entry = "write", type = WriteConfig.class, inline = true),
            @ElementList(entry = "delete", type = DeleteConfig.class, inline = true),
            @ElementList(entry = "create-child", type = CreateChildConfig.class, inline = true),
            @ElementList(entry = "execute-action", type = ExecuteActionConfig.class, inline = true),
    })
    private List<BaseOperationPermitConfig> permissions = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BaseOperationPermitConfig> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<BaseOperationPermitConfig> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccessMatrixStatusConfig that = (AccessMatrixStatusConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        if (permissions != null ? !permissions.equals(that.permissions) : that.permissions != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AccessMatrixStatusConfig [name=" + name + ", permissions=" + permissions + "]";
    }

    
    
}
