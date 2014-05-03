package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.util.HashMap;

/**
* @author Denis Mitavskiy
*         Date: 21.10.13
*         Time: 20:20
*/
class ObjectCreationOperation implements Comparable<ObjectCreationOperation> {
    public final FieldPath path; // field path of object to create (its only ID until it's physically created)

    // created object should get these references (keys) set with Ids of objects in the corresponding field paths (values)
    public final HashMap<String, FieldPath> refFieldObjectFieldPath;

    ObjectCreationOperation(FieldPath path, HashMap<String, FieldPath> refFieldObjectFieldPath) {
        this.path = path;
        this.refFieldObjectFieldPath = refFieldObjectFieldPath;
    }

    @Override
    public int compareTo(ObjectCreationOperation o) {
        for (FieldPath oRefFieldPath : o.refFieldObjectFieldPath.values()) {
            // if THAT object contains a reference field pointing to THIS field path, THIS should be the first operation
            if (path.equals(oRefFieldPath)) {
                return -1;
            }
        }
        return 0;
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
