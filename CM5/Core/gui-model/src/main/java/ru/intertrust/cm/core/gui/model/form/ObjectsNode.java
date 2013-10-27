package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.GuiException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Node of an hierarchy which may contain a list of Domain Objects
 *
 * @author Denis Mitavskiy
 *         Date: 18.10.13
 *         Time: 19:15
 */
public class ObjectsNode implements Dto, Iterable<DomainObject> {
    private String type;
    private ArrayList<DomainObject> domainObjects;

    public ObjectsNode() {
        domainObjects = new ArrayList<DomainObject>();
    }

    public ObjectsNode(int size) {
        domainObjects = new ArrayList<DomainObject>(size);
    }

    public ObjectsNode(String type, int initialSize) {
        domainObjects = new ArrayList<DomainObject>(initialSize);
        this.type = type;
    }

    public ObjectsNode(DomainObject domainObject) {
        domainObjects = new ArrayList<DomainObject>(1);
        domainObjects.add(domainObject);
        type = domainObject.getTypeName();
    }

    public String getType() {
        return type;
    }

    public DomainObject getObject() {
        if (domainObjects.size() > 1) {
            throw new GuiException("Trying to get single object from node of size: " + domainObjects.size());
        }
        return domainObjects.get(0);
    }

    public void setObject(DomainObject domainObject) {
        int size = domainObjects.size();
        if (size > 1) {
            throw new GuiException("Trying to set single object to node of size: " + size);
        }
        if (size == 0) {
            domainObjects.add(domainObject);
        } else {
            domainObjects.set(0, domainObject);
        }
    }

    // todo get rid
    @Deprecated
    public ArrayList<DomainObject> getDomainObjects() {
        return domainObjects;
    }

    @Deprecated
    public void setDomainObjects(ArrayList<DomainObject> domainObjects) {
        this.domainObjects = domainObjects;
    }

    public boolean isEmpty() {
        return domainObjects.isEmpty();
    }

    public int size() {
        return domainObjects.size();
    }

    @Override
    public Iterator<DomainObject> iterator() {
        return domainObjects.iterator();
    }

    public void add(DomainObject domainObject) {
        domainObjects.add(domainObject);
    }
}
