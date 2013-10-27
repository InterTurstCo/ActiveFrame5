package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * @author Denis Mitavskiy
 *         Date: 27.10.13
 *         Time: 17:58
 */
public class SingleObjectNode extends ObjectsNode {
    private DomainObject domainObject;

    public SingleObjectNode() {
    }

    public SingleObjectNode(String type) {
        super(type);
    }

    public SingleObjectNode(DomainObject domainObject) {
        super(domainObject.getTypeName());
        this.domainObject = domainObject;
    }

    public DomainObject getDomainObject() {
        return domainObject;
    }

    public void setDomainObject(DomainObject domainObject) {
        this.domainObject = domainObject;
    }

    public boolean isEmpty() {
        return domainObject == null;
    }
}
