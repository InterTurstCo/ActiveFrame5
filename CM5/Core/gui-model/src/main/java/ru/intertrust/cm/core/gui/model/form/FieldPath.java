package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.Collections;
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

    private static void testSort(List<FieldPath> paths) {
        Collections.sort(paths);
        for (FieldPath path : paths) {
            System.out.println(path);
        }
    }

    public static void main(String[] args) {
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
