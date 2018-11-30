package ru.intertrust.cm.core.business.api.dto;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Класс представляет собой "только время" (без даты).
 * @author Gleb Nozdrachev
 */
@ThreadSafe
@Immutable
public class TimeOnly implements Serializable, Comparable<TimeOnly> {

    private static final long serialVersionUID = 1L;

    private static int check (final int x, final int max, final @Nonnull String name) {
        if (x < 0 || x > max) {
            throw new IllegalArgumentException(name + " must be in [0.." + max + "], but provided " + x + ".");
        } else {
            return x;
        }
    }

    private final int hh;
    private final int nn;
    private final int ss;

    /**
     * Создает новый инстанс класса с указанными "часами", "минутами" и "секундами".
     * @param hh - часы [0..23];
     * @param nn - минуты [0..59];
     * @param ss - секунды [0..59];
     */
    public TimeOnly (final int hh, final int nn, final int ss) {
        this.hh = check(hh, 23, "Hours");
        this.nn = check(nn, 59, "Minutes");
        this.ss = check(ss, 59, "Seconds");
    }

    @Override
    public int hashCode () {
        return Objects.hash(this.hh, this.nn, this.ss);
    }

    @Override
    public boolean equals (final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        } else {
            final TimeOnly other = (TimeOnly)obj;
            return this.hh == other.hh && this.nn == other.nn && this.ss == other.ss;
        }
    }

    @Override
    public String toString () {
        return String.format("%02d:%02d:%02d", this.hh, this.nn, this.ss);
    }

    @Override
    public int compareTo (final TimeOnly obj) {
        int result;
        if ((result = Integer.compare(this.hh, obj.hh)) == 0 && (result = Integer.compare(this.nn, obj.nn)) == 0) {
            result = Integer.compare(this.ss, obj.ss);
        }
        return result;
    }

    /**
     * Возвращает "часы".
     */
    public int getHours () {
        return this.hh;
    }

    /**
     * Возвращает "минуты".
     */
    public int getMinutes () {
        return this.nn;
    }

    /**
     * Возвращает "секунды".
     */
    public int getSeconds () {
        return this.ss;
    }

}