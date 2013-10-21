package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.gui.model.form.FieldPath;

/**
* @author Denis Mitavskiy
*         Date: 21.10.13
*         Time: 20:20
*/
class ObjectCreationOperation implements Comparable<ObjectCreationOperation> {
    public final FieldPath path;
    public final FieldPath parentToUpdateReference;
    public final String parentField;

    ObjectCreationOperation(FieldPath path, FieldPath parentPathToUpdateReferenceIn, String parentField) {
        this.path = path;
        this.parentToUpdateReference = parentPathToUpdateReferenceIn;
        this.parentField = parentField;
    }

    @Override
    public int compareTo(ObjectCreationOperation o) {
        return -path.compareTo(o.path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ObjectCreationOperation that = (ObjectCreationOperation) o;

        if (!path.equals(that.path)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
