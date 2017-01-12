package ru.intertrust.cm.core.dao.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObject;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Предоствляет утилитарные методы для конвертации IdentifiableObject и IdentifiableObjectCollection в
 * доменный объект и коллекцию доменных объектов соответственно
 */
public class IdentifiableObjectConverter {

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    public List<DomainObject> convertToDomainObjectList(IdentifiableObjectCollection identifiableObjects) {
        if (identifiableObjects == null || identifiableObjects.size() == 0) {
            return Collections.emptyList();
        }

        List<DomainObject> result = new ArrayList<>(identifiableObjects.size());

        for (IdentifiableObject identifiableObject : identifiableObjects) {
            result.add(convertToDomainObject(identifiableObject));
        }

        return result;
    }

    public List<Id> convertToIdList(IdentifiableObjectCollection identifiableObjects) {
        if (identifiableObjects == null || identifiableObjects.size() == 0) {
            return Collections.emptyList();
        }

        List<Id> result = new ArrayList<Id>(identifiableObjects.size());

        for (IdentifiableObject identifiableObject : identifiableObjects) {
            result.add(identifiableObject.getId());
        }

        return result;
    }

    public DomainObject convertToDomainObject(IdentifiableObject identifiableObject) {
        if (identifiableObject == null) {
            return null;
        }

        GenericDomainObject domainObject = new GenericDomainObject();
        domainObject.setId(identifiableObject.getId());
        domainObject.setTypeName(domainObjectTypeIdCache.getName(identifiableObject.getId()));

        ArrayList<String> sourceFields = identifiableObject.getFields();
        for (String field : sourceFields) {
            domainObject.setValue(field, identifiableObject.getValue(field));
        }

        domainObject.resetDirty();

        return domainObject;
    }
}
