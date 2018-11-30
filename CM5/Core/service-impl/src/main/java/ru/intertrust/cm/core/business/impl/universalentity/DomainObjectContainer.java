package ru.intertrust.cm.core.business.impl.universalentity;

import java.io.Serializable;

import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.util.Args;

class DomainObjectContainer implements Serializable {

    private DomainObject domainObject;

    DomainObjectContainer (final @Nonnull DomainObject domainObject) {
        this.setDomainObject(domainObject);
    }

    @Nonnull
    DomainObject getDomainObject () {
        return this.domainObject;
    }

    void setDomainObject (final @Nonnull DomainObject domainObject) {
        this.domainObject = Args.notNull(domainObject, "domainObject");
    }

}