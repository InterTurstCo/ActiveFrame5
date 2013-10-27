package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 22.09.13
 *         Time: 13:47
 */
public class FieldPath implements Dto, Comparable<FieldPath> {
    public static final FieldPath ROOT = new FieldPath();

    private String path;
    private transient Element[] elements;
    private transient FieldPath parentPath;

    public FieldPath() {
        this.path = "";
    }

    public FieldPath(String path) {
        this.path = path;
    }

    private FieldPath(Element[] pathElements) {
        this.elements = pathElements;
    }

    public FieldPath getParentPath() {
        if (parentPath != null) {
            return parentPath;
        }

        int resultSize = getElementCount() - 1;
        if (resultSize == 0) {
            parentPath = ROOT;
            return parentPath;
        }
        Element[] newPathElements = new Element[resultSize];
        System.arraycopy(elements, 0, newPathElements, 0, resultSize);
        parentPath = new FieldPath(newPathElements);
        return parentPath;
    }

    public boolean isRoot() {
        return equals(ROOT);
    }

    public boolean isField() {
        return getLastElement() instanceof Field;
    }

    public boolean isOneToOneReference() {
        return getLastElement() instanceof OneToOneReference;
    }

    public boolean isOneToManyReference() {
        return getLastElement() instanceof OneToManyReference;
    }

    public boolean isManyToManyReference() {
        return getLastElement() instanceof ManyToManyReference;
    }

    public boolean isBackReference() {
        return getLastElement() instanceof BackReference;
    }

    public String getFieldName() {
        return getLastElement().getName();
    }

    public String getOneToOneReferenceName() {
        return getLastElement().getName();
    }

    public String getReferenceType() {
        return ((BackReference) getLastElement()).getReferenceType();
    }

    public String getLinkToParentName() {
        return ((BackReference) getLastElement()).getLinkToParentName();
    }

    public String getLinkedObjectType() {
        return ((OneToManyReference) getLastElement()).getLinkedObjectType();
    }

    public String getLinkingObjectType() {
        return ((ManyToManyReference) getLastElement()).getLinkingObjectType();
    }

    public String getLinkToChildrenName() {
        return ((ManyToManyReference) getLastElement()).getLinkToChildrenName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldPath fieldPath = (FieldPath) o;

        if (!getPath().equals(fieldPath.getPath())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    public Iterator<FieldPath> childrenIterator() {
        return new ChildPathIterator();
    }

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public int compareTo(FieldPath o) {
        return getPath().compareTo(o.getPath());
    }

    private int getElementCount() {
        return getElements().length;
    }

    private String getPath() {
        if (this.path != null) {
            return this.path;
        }
        StringBuilder result = new StringBuilder(elements.length * 5); // hard to find fields less than 4 symbols + dot
        boolean notFirst = false;
        for (Element elt : elements) {
            if (notFirst) {
                result.append('.');
            }
            result.append(elt.getName());
            notFirst = true;
        }
        this.path = result.toString();
        return this.path;
    }

    private Element[] getElements() {
        if (elements != null) {
            return elements;
        }
        String[] primitiveElements = path.split("\\.");
        int size = primitiveElements.length;
        int lastElementIndex = size - 1;
        ArrayList<Element> elements = new ArrayList<Element>(size);
        for (int i = 0; i < size; i++) {
            String elt = primitiveElements[i];
            if (elt.contains("^")) {
                if (i == lastElementIndex) {
                    elements.add(new OneToManyReference(elt));
                } else {
                    StringBuilder name = new StringBuilder(size);
                    for (int j = i; j < size; ++j) {
                        name.append(primitiveElements[j]);
                        if (j != lastElementIndex) {
                            name.append('.');
                        }
                    }
                    elements.add(new ManyToManyReference(name.toString()));
                }
                break;
            }
            if (i == lastElementIndex) {
                elements.add(new Field(elt));
            } else {
                elements.add(new OneToOneReference(elt));
            }
        }
        this.elements = elements.toArray(new Element[elements.size()]);
        return this.elements;
    }

    private Element getLastElement() {
        Element[] pathElements = getElements();
        return pathElements[pathElements.length - 1];
    }

    private FieldPath pathToChild(int childIndex) {
        Element[] childPathElements = new Element[childIndex];
        System.arraycopy(getElements(), 0, childPathElements, 0, childIndex);
        return new FieldPath(childPathElements);
    }

    public abstract static class Element implements Dto {
        protected String name;

        public Element() {
        }

        public Element(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            return name.equals(((Element) o).name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public static class Field extends Element {
        public Field() {
        }

        public Field(String name) {
            super(name);
        }

        @Override
        public String toString() {
            return name + "(Field)";
        }
    }

    public static class OneToOneReference extends Element {
        public OneToOneReference() {
        }

        public OneToOneReference(String name) {
            super(name);
        }

        @Override
        public String toString() {
            return name + "(1:1)";
        }
    }

    public static abstract class BackReference extends Element {
        protected String linkToParentName;

        protected BackReference() {
        }

        public BackReference(String name) {
            super(name);
        }

        public abstract String getReferenceType();

        public abstract String getLinkToParentName();
    }

    public static class OneToManyReference extends BackReference {
        private String linkedObjectType;

        public OneToManyReference() {
        }

        public OneToManyReference(String name) {
            super(name);
        }

        @Override
        public String getReferenceType() {
            return getLinkedObjectType();
        }

        public String getLinkedObjectType() {
            if (linkedObjectType != null) {
                return linkedObjectType;
            }
            resolveElements();
            return linkedObjectType;
        }

        public String getLinkToParentName() {
            if (linkToParentName != null) {
                return linkToParentName;
            }
            resolveElements();
            return linkToParentName;
        }

        private void resolveElements() {
            String[] linkTypeAndField = name.split("\\^");
            linkedObjectType = linkTypeAndField[0];
            linkToParentName = linkTypeAndField[1];
        }

        @Override
        public String toString() {
            return name + "(1:N)";
        }
    }

    public static class ManyToManyReference extends BackReference {
        private String linkingObjectType;
        private String linkToChildrenName;

        public ManyToManyReference() {
        }

        public ManyToManyReference(String name) {
            super(name);
        }

        @Override
        public String getReferenceType() {
            return getLinkingObjectType();
        }

        public String getLinkingObjectType() {
            if (linkingObjectType != null) {
                return linkingObjectType;
            }
            resolveElements();
            return linkingObjectType;
        }

        public String getLinkToParentName() {
            if (linkToParentName != null) {
                return linkToParentName;
            }
            resolveElements();
            return linkToParentName;
        }

        public String getLinkToChildrenName() {
            if (linkToChildrenName != null) {
                return linkToChildrenName;
            }
            resolveElements();
            return linkToChildrenName;
        }

        private void resolveElements() {
            String[] linkTypeAndField = name.split("\\^|\\.");
            linkingObjectType = linkTypeAndField[0];
            linkToParentName = linkTypeAndField[1];
            linkToChildrenName = linkTypeAndField[2];
        }

        @Override
        public String toString() {
            return name + "(N:M)";
        }
    }

    private class ChildPathIterator implements Iterator<FieldPath> {
        private int position = 0;

        @Override
        public boolean hasNext() {
            return position != getElementCount();
        }

        @Override
        public FieldPath next() {
            ++position;
            return pathToChild(position);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Field path iteration doesn't support removals.");
        }
    }

    private static void testSort(List<FieldPath> paths) {
        Collections.sort(paths);
        for (FieldPath path : paths) {
            System.out.println(path);
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString("a^b.c".split("\\^|\\.")));
        FieldPath fieldPath = new FieldPath("a.b.dflal");
        System.out.println(Arrays.toString(fieldPath.getElements()));
        ArrayList<FieldPath> paths = new ArrayList<FieldPath>();
        paths.add(new FieldPath("a.b.c.d.e"));
        paths.add(new FieldPath("a.b.c.dflal.e"));
        paths.add(new FieldPath("a.b.c.d.e.f"));
        paths.add(new FieldPath("a.b.c.dflal.e.f"));
        paths.add(new FieldPath("a.b.c.s"));
        paths.add(new FieldPath("a.b.c.t"));
        paths.add(new FieldPath("a.b.c.d"));
        paths.add(new FieldPath("a.b.c.dflal"));
        paths.add(new FieldPath("a.b.c"));
        paths.add(new FieldPath("a.b"));
        testSort(paths);
    }
}
