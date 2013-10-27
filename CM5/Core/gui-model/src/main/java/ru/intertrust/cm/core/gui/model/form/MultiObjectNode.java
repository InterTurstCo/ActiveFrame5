package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Denis Mitavskiy
 *         Date: 27.10.13
 *         Time: 17:58
 */
public class MultiObjectNode extends ObjectsNode implements Iterable<DomainObject> {
    private ArrayList<DomainObject> domainObjects;

    public MultiObjectNode() {
        domainObjects = new ArrayList<DomainObject>();
    }

    public MultiObjectNode(String type) {
        super(type);
        domainObjects = new ArrayList<DomainObject>(0);
    }

    public MultiObjectNode(String type, Collection<DomainObject> domainObjects) {
        super(type);
        this.domainObjects = new ArrayList<DomainObject>(domainObjects.size());
        this.domainObjects.addAll(domainObjects);
    }

    public ArrayList<DomainObject> getDomainObjects() {
        return domainObjects;
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
