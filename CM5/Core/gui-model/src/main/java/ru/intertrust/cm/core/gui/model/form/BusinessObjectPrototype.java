package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 21.09.13
 *         Time: 17:24
 */
public class BusinessObjectPrototype implements Dto {
    DomainObject root;
    HashMap<String, BusinessObjectPrototype> linkedObjects;
    HashMap<String, List<BusinessObjectPrototype>> linkedCollections;

    public BusinessObjectPrototype() {
        linkedObjects = new HashMap<String, BusinessObjectPrototype>(0); // todo: based on meta-data!
        linkedCollections = new HashMap<String, List<BusinessObjectPrototype>>(0);
    }

    public DomainObject getRoot() {
        return root;
    }

    public void setRoot(DomainObject domainObject) {
        this.root = domainObject;
    }

    public Value getValue(String name) {
        if (linkedObjects.containsKey(name) || linkedCollections.containsKey(name)) {
            throw new IllegalArgumentException("Name \"" + name + "\" is not a Value" );
        }

        return root.getValue(name);
    }

    public BusinessObjectPrototype getBusinessObject(String name) {
        if (root.getValue(name) != null || linkedCollections.containsKey(name)) {
            throw new IllegalArgumentException("Name \"" + name + "\" is not a Business Object" );
        }
        return linkedObjects.get(name);
    }

    public void setBusinessObject(String name, BusinessObjectPrototype businessObject) {
        linkedObjects.put(name, businessObject);
    }

    public List<BusinessObjectPrototype> getCollection(String name) {
        if (linkedObjects.containsKey(name) || root.getValue(name) != null) {
            throw new IllegalArgumentException("Name \"" + name + "\" is not a Collection" );
        }
        return linkedCollections.get(name);
    }

    public void setCollection(String name, List<BusinessObjectPrototype> businessObjects) {
        linkedCollections.put(name, businessObjects);
    }

    public Value getValueByPath(String path) { // country.city^country[0].name
        return getValueByPathElements(getPathElements(path));
    }

    public BusinessObjectPrototype getBusinessObjectByPath(String path) {
        return getBusinessObjectByPathElements(getPathElements(path));
    }

    public List<BusinessObjectPrototype> getCollectionByPath(String path) {
        return getBusinessObjectsByPathElements(getPathElements(path));
    }

    private Value getValueByPathElements(LinkedList<String> elements) {
        String elt = elements.getFirst();
        Object pathElementObject = getPathElementObject(elt);
        if (pathElementObject == null) {
            return null;
        }

        if (elements.size() == 1) {
            if (pathElementObject instanceof Value) {
                return (Value) pathElementObject;
            } else { // последний элемент является полем доменного объекта - дальше идти уже некуда
                throw new IllegalArgumentException("Path \"" + getPath(elements) + "\" doesn't correspond to a Value");
            }
        }

        if (!(pathElementObject instanceof BusinessObjectPrototype)) {
            throw new IllegalArgumentException("Path \"" + getPath(elements) + "\" doesn't correspond to Business Object. Value can be included only in another Business Object, but not in Value or Collection");
        }

        elements.removeFirst();
        return ((BusinessObjectPrototype) pathElementObject).getValueByPathElements(elements);
    }

    private BusinessObjectPrototype getBusinessObjectByPathElements(LinkedList<String> elements) {
        String elt = elements.getFirst();
        Object pathElementObject = getPathElementObject(elt);
        if (pathElementObject == null) {
            return null;
        }

        if (elements.size() == 1) {
            if (pathElementObject instanceof BusinessObjectPrototype) {
                return (BusinessObjectPrototype) pathElementObject;
            } else { // последний элемент не является бизнес-объектом - дальше уже некуда
                throw new IllegalArgumentException("Path \"" + getPath(elements) + "\" doesn't correspond to a Business Object");
            }
        }

        if (!(pathElementObject instanceof BusinessObjectPrototype)) {
            throw new IllegalArgumentException("Path \"" + getPath(elements) + "\" doesn't correspond to Business Object. Business Object can be included only in another Business Object, but not in Value or Collection");
        }

        elements.removeFirst();
        return ((BusinessObjectPrototype) pathElementObject).getBusinessObjectByPathElements(elements);
    }

    private List<BusinessObjectPrototype> getBusinessObjectsByPathElements(LinkedList<String> elements) {
        String elt = elements.getFirst();
        Object pathElementObject = getPathElementObject(elt);
        if (pathElementObject == null) {
            return null;
        }

        if (elements.size() == 1) {
            if (pathElementObject instanceof List) {
                return (List<BusinessObjectPrototype>) pathElementObject;
            } else { // последний элемент является не является коллекцией - дальше уже некуда
                throw new IllegalArgumentException("Path \"" + getPath(elements) + "\" doesn't correspond to a Collection");
            }
        }

        if (!(pathElementObject instanceof BusinessObjectPrototype)) {
            throw new IllegalArgumentException("Path \"" + getPath(elements) + "\" doesn't correspond to Business Object. Collection can be included only in another Business Object, but not in Value or Collection");
        }

        elements.removeFirst();
        return ((BusinessObjectPrototype) pathElementObject).getBusinessObjectsByPathElements(elements);
    }

    private Object getPathElementObject(String elt) {
        int bracketIndex = elt.indexOf('[');
        if (bracketIndex != -1) {
            String collectionName = elt.substring(0, bracketIndex);
            int collectionIndex = Integer.parseInt(elt.substring(bracketIndex + 1, elt.length() - 1));
            return linkedCollections.get(collectionName).get(collectionIndex);
        }
        Value value = root.getValue(elt);
        if (value != null) {
            return value;
        }

        BusinessObjectPrototype businessObject = linkedObjects.get(elt);
        if (businessObject != null) {
            return businessObject;
        }
        return linkedCollections.get(elt);
    }

    private LinkedList<String> getPathElements(String path) {
        String[] elements = path.split("\\.");
        LinkedList<String> result = new LinkedList<String>();
        Collections.addAll(result, elements);
        return result;
    }

    private String getPath(List<String> elements) {
        StringBuilder result = new StringBuilder(elements.size() * 5); // hard to find fields less than 4 symbols + dot
        boolean notFirst = false;
        for (String elt : elements) {
            if (notFirst) {
                result.append('.');
            }
            result.append(elt);
            notFirst = true;
        }
        return result.toString();
    }

    public static void main(String[] args) {
        DomainObject countryDo = new GenericDomainObject();
        countryDo.setString("name", "Russia");

        DomainObject moscowDo = new GenericDomainObject();
        moscowDo.setId(new RdbmsId(1, 1));
        moscowDo.setString("name", "Moscow");

        DomainObject nizhnijNovgorodDo = new GenericDomainObject();
        nizhnijNovgorodDo.setId(new RdbmsId(1, 1));
        nizhnijNovgorodDo.setString("name", "Nizhnij Novgorod");

        DomainObject capitalDo = new GenericDomainObject();
        capitalDo.setReference("capital", moscowDo);

        BusinessObjectPrototype capital = new BusinessObjectPrototype();
        capital.setRoot(moscowDo);

        BusinessObjectPrototype moscow = new BusinessObjectPrototype();
        moscow.setRoot(moscowDo);

        BusinessObjectPrototype nizhnijNovgorod = new BusinessObjectPrototype();
        nizhnijNovgorod.setRoot(nizhnijNovgorodDo);

        List<BusinessObjectPrototype> cities = new ArrayList<BusinessObjectPrototype>();
        cities.add(moscow);
        cities.add(nizhnijNovgorod);

        BusinessObjectPrototype country = new BusinessObjectPrototype();
        country.setRoot(countryDo);
        country.setBusinessObject("capital", capital);
        country.setCollection("cities", cities);

        System.out.println(country.getValueByPath("capital.name"));
        System.out.println(country.getValueByPath("cities[1].name"));
        //System.out.println(country.getValueByPath("cities")); // should throw exception, as there's such a collection, but not value

        //System.out.println(country.getValue("capital")); // should throw exception, as there's such a BusinessObject, but a Value
        //System.out.println(country.getCollectionByPath("capital")); // should throw exception, as it's a BO, not collection
        System.out.println(country.getBusinessObject("capital"));
        //System.out.println(country.getBusinessObject("cities")); // should throw exception, as there's such a collection
        System.out.println(country.getCollection("cities"));

        // Value name = bo.getValue("name")
        // List<BusinessObject> cities = bo.getCollection("cities") --> cities == city^country
        // Value cityName = bo.getValue("cities[0].name")
        // BusinessObject capital = bo.getBusinessObject("capital")
        // Value capitalName = bo.getValue("capital.name")

        // BusinessObject user;
        // Value lastName = user.get("last_name")
        // List<BusinessObject> roles = bo.getCollection("roles") --> roles --> user_role^user.role
        //
    }
}
