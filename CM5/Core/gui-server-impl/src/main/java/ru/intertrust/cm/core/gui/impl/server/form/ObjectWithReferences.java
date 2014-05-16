package ru.intertrust.cm.core.gui.impl.server.form;

import ru.intertrust.cm.core.gui.model.form.FieldPath;

import java.util.*;

/**
* @author Denis Mitavskiy
*         Date: 21.10.13
*         Time: 20:20
*/
class ObjectWithReferences {
    public final FieldPath path; // field path of object
    public final HashMap<String, FieldPath> references; // references this object holds to other objects

    ObjectWithReferences(FieldPath path) {
        this.path = path;
        references = new HashMap<>(2);
    }

    ObjectWithReferences(FieldPath path, HashMap<String, FieldPath> references) {
        this.path = path;
        this.references = references;
    }

    void addReference(String name, FieldPath path) {
        references.put(name, path);
    }

    void addReferencesFrom(ObjectWithReferences object) {
        if (object == null) {
            return;
        }
        references.putAll(object.references);
    }

    void fillParentReference() {
        if (path.isOneToOneBackReference()) {
            addReference(path.getLinkToParentName(), path.getParentPath());
        }
    }

    ObjectWithReferences getParentReferencingThis() {
        final ObjectWithReferences parent = new ObjectWithReferences(path.getParentPath());
        if (!path.isOneToOneBackReference()) { // direct reference
            parent.addReference(path.getFieldName(), path);
        }
        return parent;
    }

    /**
     * Sorts operations in an order for saving in database without integrity constraints violations. Original list is
     * not changed
     * @param list list of operations
     * @return sorted list
     */
    public static ArrayList<ObjectWithReferences> sortForSave(List<ObjectWithReferences> list) {
        LinkedList<ObjectWithReferences> toIterate = new LinkedList<>(list);

        ArrayList<ObjectWithReferences> result = new ArrayList<>(list.size());
        HashSet<FieldPath> allFieldPaths = new HashSet<>(list.size());
        for (ObjectWithReferences object : list) {
            allFieldPaths.add(object.path);
        }
        HashSet<FieldPath> resultFieldPaths = new HashSet<>(list.size());
        while (!toIterate.isEmpty()) {
            boolean matchFound = false;
            for (Iterator<ObjectWithReferences> iterator = toIterate.iterator(); iterator.hasNext(); ) {
                ObjectWithReferences operation = iterator.next();
                boolean allReferencesExist = true;
                for (FieldPath fieldPath : operation.references.values()) {
                    // allFieldPaths may not contain referenced path in case the object exists already, so reference to it can always be found
                    if (allFieldPaths.contains(fieldPath) && !resultFieldPaths.contains(fieldPath)) {
                        allReferencesExist = false;
                        break;
                    }
                }
                if (operation.references.isEmpty() || allReferencesExist) {
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
}
