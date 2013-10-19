package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 22.09.13
 *         Time: 13:47
 */
public class FieldPath implements Dto, Comparable<FieldPath> {
    public static final FieldPath ROOT = new FieldPath();

    private String path;
    private Boolean isBackReference;
    private transient String[] pathElements;
    private transient FieldPath parent;

    public FieldPath() {
        this.path = "";
        isBackReference = Boolean.FALSE;
    }

    public FieldPath(String path) {
        this.path = path;
    }

    public FieldPath(String[] pathElements) {
        this.pathElements = pathElements;
    }

    public FieldPath getParent() {
        if (parent != null) {
            return parent;
        }

        int resultSize = size() - 1;
        if (resultSize == 0) {
            parent = ROOT;
            return parent;
        }
        String[] newPathElements = new String[resultSize];
        System.arraycopy(pathElements, 0, newPathElements, 0, resultSize);
        parent = new FieldPath(newPathElements);
        return parent;
    }

    public String getLastElement() {
        String[] pathElements = getPathElements();
        return pathElements[pathElements.length - 1];
    }

    public int size() {
        return getPathElements().length;
    }

    public String[] getPathElements() {
        if (pathElements == null) {
            pathElements = path.split("\\.");
        }
        return pathElements;
    }

    public String getPath() {
        if (this.path != null) {
            return this.path;
        }
        StringBuilder result = new StringBuilder(pathElements.length * 5); // hard to find fields less than 4 symbols + dot
        boolean notFirst = false;
        for (String elt : pathElements) {
            if (notFirst) {
                result.append('.');
            }
            result.append(elt);
            notFirst = true;
        }
        this.path = result.toString();
        return this.path;
    }

    public FieldPath subPath(int endIndex) {
        String[] subPathElements = new String[endIndex];
        System.arraycopy(getPathElements(), 0, subPathElements, 0, endIndex);
        return new FieldPath(subPathElements);
    }

    public static List<FieldPath> toFieldPaths(List<String> paths) {
        List<FieldPath> result = new ArrayList<FieldPath>(paths.size());
        for (String path : paths) {
            result.add(new FieldPath(path));
        }
        return result;
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

    public Iterator<FieldPath> subPathIterator() {
        return new SubPathIterator();
    }

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public int compareTo(FieldPath o) {
        return getPath().compareTo(o.getPath());
    }

    private class SubPathIterator implements Iterator<FieldPath> {
        private int position = 0;

        @Override
        public boolean hasNext() {
            return position != size();
        }

        @Override
        public FieldPath next() {
            ++position;
            return subPath(position);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Field path iteration doesn't support removals.");
        }
    }

    public static void main(String[] args) {
        FieldPath emptyPath = new FieldPath("");
        System.out.println("emptyPath.size() = " + emptyPath.size());
        FieldPath fieldPath = new FieldPath("a");
        System.out.println("Original path: " + fieldPath);
        for (Iterator<FieldPath> iterator = fieldPath.subPathIterator(); iterator.hasNext(); ) {
            FieldPath subPath = iterator.next();
            System.out.println("Sub path: " + subPath);
        }

        System.out.println(fieldPath.getParent().size());

    }
}
