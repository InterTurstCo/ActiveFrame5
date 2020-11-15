package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.List;

public class DomainObjectPermission implements Dto{
    private static final long serialVersionUID = -1517544973574728720L;

    public enum Permission{
        Read,
        Write,
        Delete,
        ReadAttachment
    }
    
    private List<Permission> permissions = new ArrayList<DomainObjectPermission.Permission>();
    private List<String> actions = new ArrayList<String>();
    private List<String> createChildTypes = new ArrayList<String>();
    private Id personId;
    

    public  List<Permission> getPermission() {
        return permissions;
    }

    public void setPermission( List<Permission> permission) {
        this.permissions = permission;
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

    public List<String> getCreateChildTypes() {
        return createChildTypes;
    }

    public void setCreateChildTypes(List<String> createChildTypes) {
        this.createChildTypes = createChildTypes;
    }
}
