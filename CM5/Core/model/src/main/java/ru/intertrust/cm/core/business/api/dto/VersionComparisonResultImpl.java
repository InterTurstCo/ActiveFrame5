package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Имплементация класса информации о версии
 * @author larin
 *
 */
public class VersionComparisonResultImpl implements VersionComparisonResult {

    private List<FieldModification> modifiedFields = new ArrayList<FieldModification>();
    private Id baseVersionId;
    private Id comparedVersionId;
    private Id domainObjectId;
    private Id modifier;
    private Date modifiedDate;
    private String versionInfo;
    private String component;
    private String ipAddress;

    @Override
    public Id getBaseVersionId() {
        return baseVersionId;
    }
    
    public void setBaseVersionId(Id baseVersionId) {
        this.baseVersionId = baseVersionId;
    }

    @Override
    public Id getComparedVersionId() {
        return comparedVersionId;
    }

    public void setComparedVersionId(Id comparedVersionId) {
        this.comparedVersionId = comparedVersionId;
    }
    
    @Override
    public Id getDomainObjectId() {
        return domainObjectId;
    }

    public void setDomainObjectId(Id domainObjectId) {
        this.domainObjectId = domainObjectId;
    }
    
    @Override
    public Id getModifier() {
        return modifier;
    }

    public void setModifier(Id modifier) {
        this.modifier = modifier;
    }

    @Override
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    @Override
    public String getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }
    
    @Override
    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }
    
    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }    
    
    @Override
    public List<FieldModification> getModifiedFields() {
        return modifiedFields;
    }

    public void addFieldModification(FieldModification fieldModification) {
        modifiedFields.add(fieldModification);
    }

}
