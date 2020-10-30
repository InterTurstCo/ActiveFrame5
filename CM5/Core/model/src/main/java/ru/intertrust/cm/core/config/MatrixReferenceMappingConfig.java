package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.simpleframework.xml.ElementList;

import ru.intertrust.cm.core.business.api.dto.Dto;



public class MatrixReferenceMappingConfig implements Dto {

    private static final long serialVersionUID = -3680371232259434729L;
    
    @ElementList(inline = true, entry = "permission", required = false)
    private List<MatrixReferenceMappingPermissionConfig> permission = new ArrayList<>();

    public List<MatrixReferenceMappingPermissionConfig> getPermission() {
        return permission;
    }

    public void setPermission(List<MatrixReferenceMappingPermissionConfig> permission) {
        this.permission = permission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatrixReferenceMappingConfig that = (MatrixReferenceMappingConfig) o;
        return Objects.equals(permission, that.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permission);
    }
}
