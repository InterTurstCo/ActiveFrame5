package ru.intertrust.cm.core.business.api.dto;

/**
 * Created by Vitaliy Orlov on 03.04.2017.
 */
public class DomainObjectTypeAndAccessValue implements Dto {

    private Id objectId;
    private String domainObjectType;
    private boolean hasWritePermission;
    private boolean hasDeletePermission;
    private boolean hasReadPermission;

    public Id getObjectId() {
        return objectId;
    }

    public void setObjectId(Id objectId) {
        this.objectId = objectId;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    public boolean isHasWritePermission() {
        return hasWritePermission;
    }

    public void setHasWritePermission(boolean hasWritePermission) {
        this.hasWritePermission = hasWritePermission;
    }

    public boolean isHasDeletePermission() {
        return hasDeletePermission;
    }

    public void setHasDeletePermission(boolean hasDeletePermission) {
        this.hasDeletePermission = hasDeletePermission;
    }

    public boolean isHasReadPermission() {
        return hasReadPermission;
    }

    public void setHasReadPermission(boolean hasReadPermission) {
        this.hasReadPermission = hasReadPermission;
    }
}
