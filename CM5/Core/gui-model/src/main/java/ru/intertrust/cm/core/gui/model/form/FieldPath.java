package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 22.09.13
 *         Time: 13:47
 */
public class FieldPath implements Dto, Comparable<FieldPath> {
    public static final short MULTI_BACK_REFERENCE = 0;
    public static final short SINGLE_BACK_REFERENCE = 1;
    public static final short FIELD = 2;

    public static final FieldPath ROOT = new FieldPath();

    private static final String[] EMPTY_PATHS = new String[1];

    private String path;
    private transient String caseInsensitivePath;
    private transient Element[] elements;
    private transient FieldPath parentPath;

    public static FieldPath[] createPaths(String path) {
        String[] fieldPaths = path == null ? EMPTY_PATHS : path.split(",");
        FieldPath[] result = new FieldPath[fieldPaths.length];
        for (int i = 0; i < fieldPaths.length; ++i) {
            String fieldPath = fieldPaths[i];
            if (fieldPath == null) {
                result[i] = null;
            } else {
                fieldPath = fieldPath.trim();
                result[i] = "".equals(fieldPath) ? null : new FieldPath(fieldPath);
            }
        }
        return result;
    }

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

        if (this == ROOT) {
            return null;
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
        final Element lastElement = getLastElement();
        return lastElement instanceof OneToOneDirectReference || lastElement instanceof OneToOneBackReference;
    }

    public boolean isOneToOneDirectReference() {
        return getLastElement() instanceof OneToOneDirectReference;
    }

    public boolean isOneToOneBackReference() {
        return getLastElement() instanceof OneToOneBackReference;
    }

    public boolean isOneToManyReference() {
        return getLastElement() instanceof OneToManyReference;
    }

    public boolean isManyToManyReference() {
        return getLastElement() instanceof ManyToManyReference;
    }

    public boolean isMultiBackReference() {
        return getLastElement() instanceof MultiBackReference;
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

    public String getReferenceName() { // todo rename or drop at all. very confusing method
        MultiBackReference lastElement = (MultiBackReference) getLastElement();
        if (lastElement instanceof ManyToManyReference) {
            return ((ManyToManyReference) lastElement).getLinkToChildrenName();
        } else {
            return lastElement.getLinkToParentName();
        }
    }

    public String getLinkToParentName() {
        return ((BackReference) getLastElement()).getLinkToParentName();
    }

    public String getLinkedObjectType() {
        return ((OneToManyReference) getLastElement()).getLinkedObjectType();
    }

    public String getLinkingObjectType() {
        return ((ManyToManyReference) getLastElement()).getLinkedObjectType();
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

        if (!getCaseInsensitivePath().equals(fieldPath.getCaseInsensitivePath())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getCaseInsensitivePath().hashCode();
    }

    public Iterator<FieldPath> childrenIterator() {
        return new ChildPathIterator();
    }

    public Iterator<Element> elementsIterator() {
        return new ElementsIterator();
    }

    @Override
    public String toString() {
        return getPath();
    }

    public String toStringExtended() {
        final Element[] elements = getElements();
        StringBuilder result = new StringBuilder(elements.length * 5);
        boolean notFirst = false;
        for (Element elt : elements) {
            if (notFirst) {
                result.append('.');
            }
            result.append(elt);
            notFirst = true;
        }
        return result.toString();
    }

    @Override
    public int compareTo(FieldPath o) {
        return getPath().compareTo(o.getPath());
    }

    private int getElementCount() {
        return getElements().length;
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

    private String getCaseInsensitivePath() {
        if (this.caseInsensitivePath != null) {
            return this.caseInsensitivePath;
        }
        this.caseInsensitivePath = Case.toLower(getPath());
        return this.caseInsensitivePath;
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
            short type = resolveElement(elt);
            if (type == MULTI_BACK_REFERENCE) {
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
            if (type == SINGLE_BACK_REFERENCE) {
                elements.add(new OneToOneBackReference(elt));
                continue;
            }
            if (i == lastElementIndex) {
                elements.add(new Field(elt));
            } else {
                elements.add(new OneToOneDirectReference(elt));
            }
        }
        this.elements = elements.toArray(new Element[elements.size()]);
        return this.elements;
    }

    private static short resolveElement(final String elt) {
        final int length = elt.length();
        char symbol;
        for (int i = 0; i < length; ++i) {
            symbol = elt.charAt(i);
            if (symbol == '^') {
                return MULTI_BACK_REFERENCE;
            } else if (symbol == '|') {
                return SINGLE_BACK_REFERENCE;
            }
        }
        return FIELD;
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

    public static class OneToOneDirectReference extends Element {
        public OneToOneDirectReference() {
        }

        public OneToOneDirectReference(String name) {
            super(name);
        }

        @Override
        public String toString() {
            return name + "(1:1)";
        }
    }

    public static abstract class BackReference extends Element {
        protected String linkToParentName;
        protected String linkedObjectType;

        protected BackReference() {
        }

        public BackReference(String name) {
            super(name);
        }

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

        protected abstract void resolveElements();
    }

    public static class OneToOneBackReference extends BackReference {
        protected OneToOneBackReference() {
        }

        public OneToOneBackReference(String name) {
            super(name);
        }

        @Override
        protected void resolveElements() {
            String[] linkTypeAndField = name.split("\\|");
            linkedObjectType = linkTypeAndField[0];
            linkToParentName = linkTypeAndField[1];
        }

        @Override
        public String toString() {
            return name + "(1:1-back)";
        }
    }

    public static abstract class MultiBackReference extends BackReference {
        protected MultiBackReference() {
        }

        public MultiBackReference(String name) {
            super(name);
        }
    }

    public static class OneToManyReference extends MultiBackReference {
        public OneToManyReference() {
        }

        public OneToManyReference(String name) {
            super(name);
        }

        @Override
        protected void resolveElements() {
            String[] linkTypeAndField = name.split("\\^");
            linkedObjectType = linkTypeAndField[0];
            linkToParentName = linkTypeAndField[1];
        }

        @Override
        public String toString() {
            return name + "(1:N)";
        }
    }

    public static class ManyToManyReference extends MultiBackReference {
        private String linkToChildrenName;

        public ManyToManyReference() {
        }

        public ManyToManyReference(String name) {
            super(name);
        }

        public String getLinkToChildrenName() {
            if (linkToChildrenName != null) {
                return linkToChildrenName;
            }
            resolveElements();
            return linkToChildrenName;
        }

        @Override
        protected void resolveElements() {
            String[] linkTypeAndField = name.split("\\^|\\.");
            linkedObjectType = linkTypeAndField[0];
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

    private class ElementsIterator implements Iterator<Element> {
        private int position = 0;

        @Override
        public boolean hasNext() {
            return position != getElementCount();
        }

        @Override
        public Element next() {
            return elements[position++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Elements iteration doesn't support removals.");
        }
    }

    private static void testSort(List<FieldPath> paths) {
        Collections.sort(paths);
        for (FieldPath path : paths) {
            System.out.println(path);
        }
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(new FieldPath("x.a.b.c").getElements()));
        final Iterator<Element> elementIterator = new FieldPath("x.a.b.c").elementsIterator();
        while (elementIterator.hasNext()) {
            System.out.println(elementIterator.next());
        }
        /*System.out.println(Arrays.toString("a^b.c".split("\\^|\\.")));
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
        testSort(paths);*/
    }
}
