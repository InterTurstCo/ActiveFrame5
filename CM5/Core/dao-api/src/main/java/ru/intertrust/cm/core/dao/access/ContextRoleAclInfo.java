package ru.intertrust.cm.core.dao.access;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class ContextRoleAclInfo implements Dto {
    private String roleName;
    private List<AclInfo> aclInfos;
    
    public ContextRoleAclInfo(String roleName, List<AclInfo> aclInfos){
        this.roleName = roleName;
        this.aclInfos = aclInfos;
    }
    
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    public List<AclInfo> getAclInfos() {
        return aclInfos;
    }
    public void setAclInfos(List<AclInfo> aclInfos) {
        this.aclInfos = aclInfos;
    }

    @Override
    public String toString() {
        
        StringBuilder result = new StringBuilder();               
        result.append("\t{\n");
        result.append("\t\troleName : " + roleName + "\n");
        result.append("\t\taclInfos :\n");
        result.append("\t\t[\n");

        boolean firestRow = true;
        for (AclInfo aclInfo : aclInfos) {
            if (firestRow){
                firestRow = false;
            }else{
                result.append("\t\t\t,\n");
            }
            result.append(aclInfo.toString());
        }
        result.append("\t\t]\n");

        result.append("\t}\n");
        return result.toString();
    };
    
    
}
