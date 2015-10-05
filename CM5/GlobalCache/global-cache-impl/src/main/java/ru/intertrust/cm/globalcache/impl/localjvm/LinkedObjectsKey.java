package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.globalcache.impl.util.Size;
import ru.intertrust.cm.globalcache.impl.util.SizeEstimator;
import ru.intertrust.cm.globalcache.impl.util.Sizeable;

/**
 * @author Denis Mitavskiy
 *         Date: 09.07.2015
 *         Time: 18:24
 */
public class LinkedObjectsKey implements Sizeable {
    public final String type;
    public final String field;
    public final boolean exactType;
    public final Size size;

    public LinkedObjectsKey(String type, String field, boolean exactType) {
        this.type = type.toLowerCase();
        this.field = field.toLowerCase();
        this.exactType = exactType;
        this.size = new Size().set(SizeEstimator.estimateSize(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkedObjectsKey that = (LinkedObjectsKey) o;

        if (exactType != that.exactType) {
            return false;
        }
        if (!type.equals(that.type)) {
            return false;
        }
        if (!field.equals(that.field)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + field.hashCode();
        result = 31 * result + (exactType ? 1 : 0);
        return result;
    }

    @Override
    public void setSizeTotal(Size total) {
        size.setTotal(total);
    }

    @Override
    public Size getSize() {
        return size;
    }
}
