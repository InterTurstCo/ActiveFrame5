package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

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
    public final Id id; // for delete operations
    public final String fieldToSetWithRootReference;

    FormSaveOperation(Type type, DomainObject domainObject, String fieldToSetWithRootReference) {
        this.type = type;
        this.domainObject = domainObject;
        this.id = domainObject.getId();
        this.fieldToSetWithRootReference = fieldToSetWithRootReference;
    }

    FormSaveOperation(Type type, Id id) {
        this.type = type;
        this.domainObject = null;
        this.id = id;
        this.fieldToSetWithRootReference = null;
    }
}
