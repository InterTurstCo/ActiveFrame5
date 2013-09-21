package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 21.09.13
 *         Time: 17:24
 */
public class BusinessObject implements Dto {
    DomainObject root;
    HashMap<String, BusinessObject> linkedObjects;
    HashMap<String, List<BusinessObject>> linkedCollections;

    public BusinessObject() {
        linkedObjects = new HashMap<String, BusinessObject>(0); // todo: based on meta-data!
        linkedCollections = new HashMap<String, List<BusinessObject>>(0);
    }

    public DomainObject getRoot() {
        return root;
    }

    public void setRoot(DomainObject domainObject) {
        this.root = domainObject;
    }

    public Value getValue(String name) {
        return root.getValue(name);
    }

    public BusinessObject getBusinessObject(String name) {
        return linkedObjects.get(name);
    }

    public void setBusinessObject(String name, BusinessObject businessObject) {
        linkedObjects.put(name, businessObject);
    }

    public List<BusinessObject> getBusinessObjects(String name) {
        return linkedCollections.get(name);
    }

    public void setBusinessObjects(String name, List<BusinessObject> businessObjects) {
        linkedCollections.put(name, businessObjects);
    }

    public Value getValueByPath(String path) { // country.city^country[0].name
        String[] elements = path.split("\\.");
        return getValueByPathElements(elements);
    }

    public BusinessObject getBusinessObjectByPath() {
        return null;
    }

    public List<BusinessObject> getBusinessObjectsByPath(String path) {
        return null;
    }

    private Value getValueByPathElements(String[] elements) {
        String elt = elements[0];
        Object pathElementObject = getPathElementObject(elt);
        if (pathElementObject == null) {
            return null;
        }

        if (elements.length == 1) {
            if (pathElementObject instanceof Value) {
                return (Value) pathElementObject;
            } else { // не последний элемент является полем доменного объекта - дальше уже некуда
                throw new IllegalArgumentException("Path " + getPath(elements) + " doesn't correspond to a Value");
            }
        }

        if (pathElementObject instanceof List) {
            throw new IllegalArgumentException("Path " + getPath(elements) + " corresponds to a Collection. Value can't be retrieved without collection index. Use getBusinessObjectsByPath() instead.");
        }

        int newLength = elements.length - 1;
        String[] subPath = new String[newLength];
        System.arraycopy(elements, 1, subPath, 0, newLength);
        return ((BusinessObject) pathElementObject).getValueByPathElements(subPath);
    }

    private Object getPathElementObject(String elt) {
        int bracketIndex = elt.indexOf('[');
        if (bracketIndex != -1) {
            String collectionName = elt.substring(0, bracketIndex);
            int collectionIndex = Integer.parseInt(elt.substring(bracketIndex + 1, elt.length() - 1));
            return linkedCollections.get(collectionName).get(collectionIndex);
        }
        Value value = getValue(elt);
        if (value != null) {
            return value;
        }

        return linkedObjects.get(elt);
    }

    private String getPath(String[] elements) {
        StringBuilder result = new StringBuilder(elements.length * 5); // hard to find fields less than 4 symbols + dot
        for (int i = 0; i < elements.length; ++i) {
            String elt = elements[i];
            if (i != 0) {
                result.append('.');
            }
            result.append(elt);
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

        BusinessObject capital = new BusinessObject();
        capital.setRoot(moscowDo);

        BusinessObject moscow = new BusinessObject();
        moscow.setRoot(moscowDo);

        BusinessObject nizhnijNovgorod = new BusinessObject();
        nizhnijNovgorod.setRoot(nizhnijNovgorodDo);

        List<BusinessObject> cities = new ArrayList<BusinessObject>();
        cities.add(moscow);
        cities.add(nizhnijNovgorod);

        BusinessObject country = new BusinessObject();
        country.setRoot(countryDo);
        country.setBusinessObject("capital", capital);
        country.setBusinessObjects("cities", cities);

        System.out.println(country.getValueByPath("capital.name"));
        System.out.println(country.getValueByPath("cities[1].name"));

        // Value name = bo.getValue("name")
        // List<BusinessObject> cities = bo.getBusinessObjects("cities") --> cities == city^country
        // Value cityName = bo.getValue("cities[0].name")
        // BusinessObject capital = bo.getBusinessObject("capital")
        // Value capitalName = bo.getValue("capital.name")

        // BusinessObject user;
        // Value lastName = user.get("last_name")
        // List<BusinessObject> roles = bo.getBusinessObjects("roles") --> roles --> user_role^user.role
        //
    }
}
