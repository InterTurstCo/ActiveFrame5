package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

public class CreateNewRegionActionData extends ActionData {
    private DomainObject rootDomainObject;

    public DomainObject getRootDomainObject() {
        return rootDomainObject;
    }

    public void setRootDomainObject(DomainObject rootDomainObject) {
        this.rootDomainObject = rootDomainObject;
    }
}
