package ru.intertrust.cm.core.business.impl.profiling;

/**
 * @author Denis Mitavskiy
 *         Date: 03.02.2017
 *         Time: 18:58
 */
public class ThreadInfo implements Comparable<ThreadInfo> {
    public final long id;
    public final String name;
    public final Thread.State state;

    public ThreadInfo(Thread thread) {
        this.id = thread.getId();
        this.name = thread.getName();
        this.state = thread.getState();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThreadInfo that = (ThreadInfo) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int compareTo(ThreadInfo o) {
        return Long.compare(this.id, o.id);
    }

    @Override
    public String toString() {
        return id + " (" + name + ")";
    }
}
