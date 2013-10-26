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
    private Boolean isBackReference;
    private transient Element[] elements;
    private transient FieldPath parentPath;

    public FieldPath() {
        this.path = "";
        isBackReference = Boolean.FALSE;
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

        int resultSize = getElementsCount() - 1;
        if (resultSize == 0) {
            parentPath = ROOT;
            return parentPath;
        }
        Element[] newPathElements = new Element[resultSize];
        System.arraycopy(elements, 0, newPathElements, 0, resultSize);
        parentPath = new FieldPath(newPathElements);
        return parentPath;
    }

    public Element getLastElement() {
        Element[] pathElements = getElements();
        return pathElements[pathElements.length - 1];
    }

    public int getElementsCount() {
        return getElements().length;
    }

    public Element[] getElements() {
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
                    elements.add(new OneToManyBackReference(elt));
                    break;
                } else {
                    elements.add(new ManyToManyReference(elt));
                    continue;
                }
            }
            if (i == lastElementIndex) {
                elements.add(new Field(elt));
            } else {
                elements.add(new OneToOneReference(elt));
            }
        }
        // todo This is to use after!
        /*for (int i = 0; i < size; i++) {
            String elt = primitiveElements[i];
            if (elt.contains("^")) {
                if (i == lastElementIndex) {
                    elements.add(new OneToManyBackReference(elt));
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
        }*/
        this.elements = elements.toArray(new Element[elements.size()]);
        return this.elements;
    }

    public String getPath() {
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

    private FieldPath pathToChild(int childIndex) {
        Element[] childPathElements = new Element[childIndex];
        System.arraycopy(getElements(), 0, childPathElements, 0, childIndex);
        return new FieldPath(childPathElements);
    }

    public boolean isRoot() {
        return equals(ROOT);
    }

    public boolean isBackReference() {
        if (isBackReference == null) {
            isBackReference = getPath().contains("^");
        }
        return isBackReference;
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

    public Iterator<FieldPath> childPathIterator() {
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

    public static class OneToManyBackReference extends Element {
        public OneToManyBackReference() {
        }

        public OneToManyBackReference(String name) {
            super(name);
        }

        @Override
        public String toString() {
            return name + "(1:N)";
        }
    }

    public static class ManyToManyReference extends Element {
        public ManyToManyReference() {
        }

        public ManyToManyReference(String name) {
            super(name);
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
            return position != getElementsCount();
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
