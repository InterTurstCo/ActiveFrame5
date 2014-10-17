package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;

import ru.intertrust.cm.core.business.api.dto.Dto;



public class MatrixReferenceMappingConfig implements Dto {

    private static final long serialVersionUID = -3680371232259434729L;
    
    @ElementList(inline = true, entry = "permition", required = false)
    private List<MatrixReferenceMappingPermissionConfig> permission = new ArrayList<>();

    public List<MatrixReferenceMappingPermissionConfig> getPermission() {
        return permission;
    }

    public void setPermission(List<MatrixReferenceMappingPermissionConfig> permission) {
        this.permission = permission;
    }


}
