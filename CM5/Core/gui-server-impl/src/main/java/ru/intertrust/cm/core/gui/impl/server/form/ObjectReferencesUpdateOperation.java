package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.util.HashMap;

/**
* @author Denis Mitavskiy
*         Date: 02.05.14
*         Time: 22:17
*/
class ObjectReferencesUpdateOperation implements Comparable<ObjectReferencesUpdateOperation> {
    public final FieldPath path; // field path of object to update (its only ID until it's physically created)
    // object should get these references (keys) set with Ids of objects in the corresponding field paths (values)
    public final HashMap<String, FieldPath> refFieldObjectFieldPath;

    ObjectReferencesUpdateOperation(FieldPath path, HashMap<String, FieldPath> refFieldObjectFieldPath) {
        this.path = path;
        this.refFieldObjectFieldPath = refFieldObjectFieldPath;
    }

    @Override
    public int compareTo(ObjectReferencesUpdateOperation o) {
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

        ObjectReferencesUpdateOperation that = (ObjectReferencesUpdateOperation) o;

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
