package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

public class GenericDomainObjectVersion extends GenericIdentifiableObject implements DomainObjectVersion{
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3972306457979921953L;
    
    /**
     * Идентификатор доменного объекта
     */    
    private Id domainObjectId;

    /**
     * Дата изменения
     */
    private Date modifiedDate;

    /**
     * Операция
     */
    private AuditLogOperation operation;

    /**
     * Информация о версии
     */
    private String versionInfo;

    /**
     * Информации о компоненте, внесшей изменения
     */
    private String component;

    /**
     * Информация о IP адресе
     */
    private String ipAddress;

    /**
     * Идентификатор персоны, инициировавшей изменения ДО.
     */
    private Id modifier;

    @Override
    public Id getDomainObjectId() {
        return domainObjectId;
    }

    public void setDomainObjectId(Id domainObjectId){
        this.domainObjectId = domainObjectId;
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
    public AuditLogOperation getOperation() {
        return operation;
    }
    
    public void setOperation(AuditLogOperation operation) {
        this.operation = operation;
    }

    public GenericDomainObjectVersion() {
    }

    public GenericDomainObjectVersion(IdentifiableObject source) {
        super(source);
    }
}
