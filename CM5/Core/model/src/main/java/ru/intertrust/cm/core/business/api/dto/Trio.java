package ru.intertrust.cm.core.business.api.dto;

/**
 * @author Denis Mitavskiy
 *         Date: 12.10.13
 *         Time: 18:58
 */
public class Trio<T1, T2, T3> implements Dto {
    private T1 first;
    private T2 second;
    private T3 third;

    public Trio() {
    }

    public Trio(T1 first, T2 second, T3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T1 getFirst() {
        return first;
    }

    public void setFirst(T1 first) {
        this.first = first;
    }

    public T2 getSecond() {
        return second;
    }

    public void setSecond(T2 second) {
        this.second = second;
    }

    public T3 getThird() {
        return third;
    }

    public void setThird(T3 third) {
        this.third = third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Trio trio = (Trio) o;

        if (first != null ? !first.equals(trio.first) : trio.first != null) {
            return false;
        }
        if (second != null ? !second.equals(trio.second) : trio.second != null) {
            return false;
        }
        if (third != null ? !third.equals(trio.third) : trio.third != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }
}
