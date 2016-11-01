package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ObjectCloner;

import java.util.*;

/**
 * Сущность для работы с изменениями доменных объектов
 */
public class DomainObjectsModification implements Dto {

    private String transactionId;
    private Map<Id, Map<String, FieldModification>> savedDomainObjectsModificationMap = new HashMap<>();
    private Map<Id, DomainObject> savedDomainObjects = new HashMap<>();
    private List<DomainObject> createdDomainObjects = new ArrayList<>();
    private List<Id> modifiedAutoDomainObjectIds = new ArrayList<>();
    private Map<Id, DomainObject> deletedDomainObjects = new HashMap<>();
    private Map<Id, DomainObject> changeStatusDomainObjects = new LinkedHashMap<>();
    private Map<Id, DomainObject> savedAndChangedStatusDomainObjects = new LinkedHashMap<>();

    public DomainObjectsModification(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public boolean isEmpty() {
        return createdDomainObjects.isEmpty() && deletedDomainObjects.isEmpty() && savedAndChangedStatusDomainObjects.isEmpty() && modifiedAutoDomainObjectIds.isEmpty();
    }

    public List<DomainObject> getCreatedDomainObjects() {
        return createdDomainObjects;
    }

    public List<Id> getCreatedIds() {
        ArrayList<Id> created = new ArrayList<>(createdDomainObjects.size());
        for (DomainObject object : createdDomainObjects) {
            created.add(object.getId());
        }
        return created;
    }

    public List<Id> getChangeStatusDomainObjectIds() {
        return new ArrayList<>(changeStatusDomainObjects.keySet());
    }

    public boolean wasSaved(Id id) {
        return savedDomainObjects.containsKey(id);
    }

    public Collection<DomainObject> getSavedDomainObjects() {
        return savedDomainObjects.values();
    }

    public Collection<DomainObject> getSavedAndChangedStatusDomainObjects() {
        return savedAndChangedStatusDomainObjects.values();
    }

    public Set<Id> getSavedAndChangedStatusIds() {
        return savedAndChangedStatusDomainObjects.keySet();
    }

    public Collection<DomainObject> getDeletedDomainObjects() {
        return deletedDomainObjects.values();
    }

    public List<Id> getModifiedAutoDomainObjectIds() {
        return modifiedAutoDomainObjectIds;
    }

    public Set<Id> getDeletedIds() {
        return deletedDomainObjects.keySet();
    }

    public void addCreatedDomainObject(DomainObject domainObject) {
        domainObject = ObjectCloner.fastCloneDomainObject(domainObject);

        createdDomainObjects.add(domainObject);
    }

    public void addModifiedAutoDomainObject(Id id) {
        modifiedAutoDomainObjectIds.add(id); // do not clone as developers do not get these objects at their disposal
    }

    public void addChangeStatusDomainObject(DomainObject domainObject){
        final Id id = domainObject.getId();
        domainObject = ObjectCloner.fastCloneDomainObject(domainObject);

        savedAndChangedStatusDomainObjects.put(id, domainObject);
        if (!changeStatusDomainObjects.containsKey(id)){
            changeStatusDomainObjects.put(id, domainObject);
        }
    }

    public void addDeletedDomainObject(DomainObject domainObject){
        if (deletedDomainObjects.get(domainObject.getId()) == null){
            deletedDomainObjects.put(domainObject.getId(), domainObject);
        }
    }

    public void addSavedDomainObject(DomainObject domainObject, List<FieldModification> newFields) {
        domainObject = ObjectCloner.fastCloneDomainObject(domainObject);

        savedDomainObjects.put(domainObject.getId(), domainObject);
        savedAndChangedStatusDomainObjects.put(domainObject.getId(), domainObject);

        //Ишем не сохраняли ранее
        Map<String, FieldModification> fields = savedDomainObjectsModificationMap.get(domainObject.getId());
        if (fields == null) {
            fields = new HashMap<>();
            savedDomainObjectsModificationMap.put(domainObject.getId(), fields);
        }

        //Мержим информацию об измененных полях
        for (FieldModification newFieldModification : newFields) {
            FieldModificationImpl registeredFieldModification = (FieldModificationImpl) fields.get(newFieldModification.getName());
            if (registeredFieldModification == null) {
                registeredFieldModification = new FieldModificationImpl(newFieldModification.getName(),
                        newFieldModification.getBaseValue(), newFieldModification.getComparedValue());
                fields.put(newFieldModification.getName(), registeredFieldModification);
            } else {
                registeredFieldModification.setComparedValue(newFieldModification.getComparedValue());
            }
        }
    }

    public List<FieldModification> getFieldModificationList(Id id) {
        Map<String, FieldModification> map = savedDomainObjectsModificationMap.get(id);
        List<FieldModification> result = new ArrayList<>(map.size());
        for (FieldModification field : map.values()) {
            result.add(field);
        }
        return result;
    }
}
