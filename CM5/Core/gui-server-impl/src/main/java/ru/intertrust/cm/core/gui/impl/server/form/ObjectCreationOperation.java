package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.util.*;

/**
* @author Denis Mitavskiy
*         Date: 21.10.13
*         Time: 20:20
*/
class ObjectCreationOperation {
    public final FieldPath path; // field path of object to create (its only ID until it's physically created)

    // created object should get these references (keys) set with Ids of objects in the corresponding field paths (values)
    public final HashMap<String, FieldPath> refFieldObjectFieldPath;

    ObjectCreationOperation(FieldPath path, HashMap<String, FieldPath> refFieldObjectFieldPath) {
        this.path = path;
        this.refFieldObjectFieldPath = refFieldObjectFieldPath;
    }

    /**
     * Sorts operations in an order for saving in database without integrity constraints violations. Original list is
     * not changed
     * @param list list of operations
     * @return sorted list
     */
    public static ArrayList<ObjectCreationOperation> sortForSave(List<ObjectCreationOperation> list) {
        LinkedList<ObjectCreationOperation> toIterate = new LinkedList<>(list);

        ArrayList<ObjectCreationOperation> result = new ArrayList<>(list.size());
        HashSet<FieldPath> resultFieldPaths = new HashSet<>(list.size());
        while (!toIterate.isEmpty()) {
            boolean matchFound = false;
            for (Iterator<ObjectCreationOperation> iterator = toIterate.iterator(); iterator.hasNext(); ) {
                ObjectCreationOperation operation = iterator.next();
                if (operation.refFieldObjectFieldPath.isEmpty() || resultFieldPaths.containsAll(operation.refFieldObjectFieldPath.values())) {
                    result.add(operation);
                    resultFieldPaths.add(operation.path);
                    iterator.remove();
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                throw new IllegalArgumentException("Not possible to order list for saving in database");
            }
        }
        return result;
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
