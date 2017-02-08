package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainEntitiesCloner;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 07.02.2017
 *         Time: 18:13
 */
public class DomainEntitiesClonerImpl implements DomainEntitiesCloner {
    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    public DomainObject fastCloneDomainObject(DomainObject domainObject) {
        if (domainObject == null) {
            return null;
        }
        final Set<String> fields = configurationExplorer.getDomainObjectTypeAllFieldNames(domainObject.getTypeName());
        final GenericDomainObject clone = new GenericDomainObject(domainObject);
        clone.retainFields(fields);
        final LinkedHashMap<String, Value> fieldValues = clone.getFieldValues();
        new MapValueNormalizer().normalize(fieldValues.entrySet().iterator());
        clone.setId(fastCloneId(clone.getId()));

        clone.resetDirty();
        return clone;
    }

    @Override
    public List<DomainObject> fastCloneDomainObjectList(List<DomainObject> domainObjects) {
        if (domainObjects == null) {
            return null;
        }
        ArrayList<DomainObject> result = new ArrayList<>(domainObjects.size());
        for (DomainObject domainObject : domainObjects) {
            result.add(fastCloneDomainObject(domainObject));
        }
        return result;
    }

    @Override
    public IdentifiableObjectCollection fastCloneCollection(IdentifiableObjectCollection collection) {
        if (collection == null) {
            return null;
        }
        try {
            return (IdentifiableObjectCollection) ((GenericIdentifiableObjectCollection) collection).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Filter fastCloneFilter(final Filter filter) {
        if (filter == null) {
            return null;
        }
        final Filter cloned = filter.clone();
        for (Map.Entry<Integer, List<Value>> entry : cloned.getParameterMap().entrySet()) {
            entry.setValue(fastCloneValueList(entry.getValue()));
        }
        return cloned;
    }

    @Override
    public SortOrder fastCloneSortOrder(final SortOrder sortOrder) {
        if (sortOrder == null) {
            return null;
        }
        try {
            SortOrder clone = new SortOrder();
            for (SortCriterion sortCriterion : sortOrder) {
                clone.add((SortCriterion) sortCriterion.clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public List<Value> fastCloneValueList(final List<? extends Value> values) {
        if (values == null) {
            return null;
        }
        final ArrayList<Value> clone = new ArrayList<>(values);
        new ListValueNormalizer().normalize(clone.listIterator());
        return clone;
    }

    @Override
    public Id fastCloneId(final Id id) {
        if (id == null) {
            return null;
        }
        if (id instanceof RdbmsId && id.getClass() != RdbmsId.class) {
            return new RdbmsId(id);
        } else {
            return id;
        }
    }

    private abstract class ValueNormalizer {
        protected abstract Value getValue(Object iterationObject);
        protected abstract void replaceValue(Iterator iterator, Object iterationObject, Value newValue);

        public void normalize(Iterator iterator) {
            ObjectCloner cloner = null;
            while (iterator.hasNext()) {
                final Object iterationObject = iterator.next();
                final Value value = getValue(iterationObject);
                if (value != null) {
                    if (value instanceof ReferenceValue && value.getClass() != ReferenceValue.class) {
                        replaceValue(iterator, iterationObject, new ReferenceValue(fastCloneId((Id) value.get())));
                        continue;
                    }
                    if (!value.isImmutable()) {
                        if (cloner == null) {
                            cloner = ObjectCloner.getInstance();
                        }
                        replaceValue(iterator, iterationObject, cloner.cloneObject(value));
                    }
                }
            }
        }
    }

    private class ListValueNormalizer extends ValueNormalizer {

        @Override
        protected Value getValue(Object iterationObject) {
            return (Value) iterationObject;
        }

        @Override
        protected void replaceValue(Iterator iterator, Object iterationObject, Value newValue) {
            ((ListIterator) iterator).set(newValue);
        }
    }

    private class MapValueNormalizer extends ValueNormalizer {

        @Override
        protected Value getValue(Object iterationObject) {
            return ((Map.Entry<?, Value>) iterationObject).getValue();
        }

        @Override
        protected void replaceValue(Iterator iterator, Object iterationObject, Value newValue) {
            ((Map.Entry<?, Value>) iterationObject).setValue(newValue);
        }
    }
}
