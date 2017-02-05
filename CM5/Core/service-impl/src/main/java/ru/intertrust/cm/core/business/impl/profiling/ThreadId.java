package ru.intertrust.cm.core.business.impl.profiling;

/**
 * @author Denis Mitavskiy
 *         Date: 03.02.2017
 *         Time: 18:58
 */
public class ThreadId implements Comparable<ThreadId> {
    public final long id;
    public final String name;

    public ThreadId(Thread thread) {
        this(thread.getId(), thread.getName());
    }

    public ThreadId(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThreadId that = (ThreadId) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int compareTo(ThreadId o) {
        return Long.compare(this.id, o.id);
    }

    @Override
    public String toString() {
        return id + " (" + name + ")";
    }
}
