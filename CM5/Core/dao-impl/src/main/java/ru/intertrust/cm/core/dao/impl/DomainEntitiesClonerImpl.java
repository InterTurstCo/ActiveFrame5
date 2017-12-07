package ru.intertrust.cm.core.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainEntitiesCloner;

import java.util.*;

/**
 * @author Denis Mitavskiy Date: 07.02.2017 Time: 18:13
 */
public class DomainEntitiesClonerImpl implements DomainEntitiesCloner {
    private final static Set<Class<? extends Value>> PLATFORM_VALUE_CLASSES;

    static {
        PLATFORM_VALUE_CLASSES = new HashSet<>();
        PLATFORM_VALUE_CLASSES.add(StringValue.class);
        PLATFORM_VALUE_CLASSES.add(LongValue.class);
        PLATFORM_VALUE_CLASSES.add(BooleanValue.class);
        PLATFORM_VALUE_CLASSES.add(DecimalValue.class);
        PLATFORM_VALUE_CLASSES.add(DateTimeWithTimeZoneValue.class);
        PLATFORM_VALUE_CLASSES.add(DateTimeValue.class);
        PLATFORM_VALUE_CLASSES.add(TimelessDateValue.class);
        PLATFORM_VALUE_CLASSES.add(ReferenceValue.class);
    }

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Override
    public DomainObject fastCloneDomainObject(DomainObject domainObject) {
        if (domainObject == null) {
            return null;
        }
        if (domainObject instanceof GenericDomainObject) {
            return cloneGenericDomainObject((GenericDomainObject) domainObject);
        } else {
            return new GenericDomainObject(domainObject);
        }
    }

    public DomainObject cloneGenericDomainObject(GenericDomainObject domainObject) {
        final Set<String> fieldsLowerCased = configurationExplorer.getDomainObjectTypeAllFieldNamesLowerCased(domainObject.getTypeName());
        LinkedHashSet<String> originalKeys = domainObject.getOriginalKeys();
        LinkedHashMap<String, Value> fieldValues = domainObject.getFieldValues();

        LinkedHashSet<String> newOriginalKeys = new LinkedHashSet<>((int) (originalKeys.size() / 0.75));
        LinkedHashMap<String, Value> newFieldValues = new LinkedHashMap<>((int) (fieldValues.size() / 0.75));

        for (String originalKey : originalKeys) {
            String originalKeyLower = Case.toLower(originalKey);
            if (fieldsLowerCased.contains(originalKeyLower)) {
                newOriginalKeys.add(originalKey);
                newFieldValues.put(originalKeyLower, fieldValues.get(originalKeyLower));
            }
        }
        new MapValueNormalizer().normalize(fieldValues.entrySet().iterator(), false);
        Id clonedId = fastCloneId(domainObject.getId());
        return new GenericDomainObject(clonedId, domainObject.getTypeName(), newOriginalKeys, newFieldValues);
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
        new ListValueNormalizer().normalize(clone.listIterator(), true);
        return clone;
    }

    @Override
    public Id fastCloneId(final Id id) {
        if (id == null) {
            return null;
        }
        if (id.getClass() != RdbmsId.class) {
            return new RdbmsId(id);
        } else {
            return id;
        }
    }

    private static boolean isPlatformValue(Value value) {
        return PLATFORM_VALUE_CLASSES.contains(value.getClass());
    }

    private static ListValue cloneListValue(ListValue listValue) {
        final List<Value<?>> values = listValue.getUnmodifiableValuesList();
        final ArrayList<Value<?>> clone = new ArrayList<>(values.size());
        for (Value value : values) {
            if (value == null) {
                clone.add(null);
            } else {
                final Value platformClone = value.getPlatformClone();
                if (platformClone == null) {
                    return null;
                }
                clone.add(platformClone);
            }
        }
        return ListValue.createListValue(clone);
    }

    private abstract class ValueNormalizer {
        protected abstract Value getValue(Object iterationObject);

        protected abstract void replaceValue(Iterator iterator, Object iterationObject, Value newValue);

        public void normalize(Iterator iterator, boolean supportListValue) {
            ObjectCloner cloner = null;
            while (iterator.hasNext()) {
                final Object iterationObject = iterator.next();
                final Value value = getValue(iterationObject);
                if (value != null) {
                    Value clone;
                    if (isPlatformValue(value)) {
                        clone = value.getPlatformClone();
                    } else if (supportListValue && value instanceof ListValue) {
                        clone = cloneListValue((ListValue) value);
                    } else {
                        clone = null;
                    }
                    replaceValue(iterator, iterationObject, clone);
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
