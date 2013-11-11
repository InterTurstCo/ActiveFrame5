package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.List;

public class DomainObjectPermission implements Dto{
    
    public enum Permission{
        None,
        Read,
        Write,
        Delate
    }
    
    private Permission permission = Permission.None;
    private List<String> actions = new ArrayList<String>();
    private Id personId;
    

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public Id getPersonId() {
        return personId;
    }

    public void setPersonId(Id personId) {
        this.personId = personId;
    }
}
