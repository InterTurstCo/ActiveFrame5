package ru.intertrust.cm.core.business.api.dto;

import java.util.*;

/**
 * Сущность для работы с изменениями доменных объектов
 */
public class DomainObjectsModification implements Dto {

    private String transactionId;
    private Map<Id, Map<String, FieldModification>> savedDomainObjectsModificationMap = new HashMap<>();
    private Map<Id, DomainObject> savedDomainObjects = new HashMap<>();
    private List<DomainObject> createdDomainObjects = new ArrayList<>();
    private Map<Id, DomainObject> deletedDomainObjects = new HashMap<>();
    private Map<Id, DomainObject> changeStatusDomainObjects = new LinkedHashMap<>();
    private Map<Id, DomainObject> savedAndChangedStatusDomainObjects = new LinkedHashMap<>();

    public DomainObjectsModification(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public List<DomainObject> getCreatedDomainObjects() {
        return createdDomainObjects;
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

    public Collection<DomainObject> getDeletedDomainObjects() {
        return deletedDomainObjects.values();
    }

    public void addCreatedDomainObject(DomainObject domainObject){
        createdDomainObjects.add(domainObject);
    }

    public void addChangeStatusDomainObject(DomainObject domainObject){
        final Id id = domainObject.getId();
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
        savedDomainObjects.put(domainObject.getId(), domainObject);
        savedAndChangedStatusDomainObjects.put(domainObject.getId(), domainObject);

        //Ишем не сохраняли ранее
        Map<String, FieldModification> fields = savedDomainObjectsModificationMap.get(domainObject.getId());
        if (fields == null){
            fields = new HashMap<>();
            savedDomainObjectsModificationMap.put(domainObject.getId(), fields);
        }

        //Мержим информацию об измененных полях
        for (FieldModification newFieldModification : newFields) {
            FieldModificationImpl registeredFieldModification = (FieldModificationImpl)fields.get(newFieldModification.getName());
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
