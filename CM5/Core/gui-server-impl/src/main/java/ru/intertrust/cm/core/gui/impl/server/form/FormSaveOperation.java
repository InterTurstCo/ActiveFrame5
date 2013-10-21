package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
* @author Denis Mitavskiy
*         Date: 21.10.13
*         Time: 20:20
*/
class FormSaveOperation {
    public static enum Type {
        Create,
        Update,
        Delete
    }

    public final Type type;
    public final DomainObject domainObject;
    public final String fieldToSetWithRootReference;

    FormSaveOperation(Type type, DomainObject domainObject, String fieldToSetWithRootReference) {
        this.type = type;
        this.domainObject = domainObject;
        this.fieldToSetWithRootReference = fieldToSetWithRootReference;
    }
}
