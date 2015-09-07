package ru.intertrust.cm.core.dao.access;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class AclData implements Dto{
    private List<ContextRoleAclInfo> contextRoleAclInfo = new ArrayList<ContextRoleAclInfo>();

    public List<ContextRoleAclInfo> getContextRoleAclInfo() {
        return contextRoleAclInfo;
    }

    public void setContextRoleAclInfo(List<ContextRoleAclInfo> contextRoleAclInfo) {
        this.contextRoleAclInfo = contextRoleAclInfo;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();               
        result.append("[\n");
        boolean firestRow = true;
        for (ContextRoleAclInfo item : contextRoleAclInfo) {
            if (firestRow){
                firestRow = false;
            }else{
                result.append("\t,\n");
            }
            result.append(item.toString());
        }
        result.append("]\n");
        return result.toString();
    } 
    
    
}
