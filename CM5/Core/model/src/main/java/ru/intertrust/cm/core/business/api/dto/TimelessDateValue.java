package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.model.GwtIncompatible;

import java.util.Date;
import java.util.TimeZone;

/**
 * Значение поля доменного объекта, определяющее дату без времени время в виде календаря.
 * @author vmatsukevich
 *         Date: 10/24/13
 *         Time: 3:29 PM
 */
public class TimelessDateValue extends Value<TimelessDateValue> {
    private TimelessDate value;

    /**
     * Создает пустое булево значение
     */
    public TimelessDateValue() {
    }

    @GwtIncompatible
    public TimelessDateValue(Date date, TimeZone timeZone) {
        if (date == null) {
            return;
        }
        value = new TimelessDate(date, timeZone);
    }

    /**
     * Создает булево значение
     * @param value булево значение
     */
    public TimelessDateValue(TimelessDate value) {
        this.value = value;
    }

    @Override
    public TimelessDate get() {
        return value;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public int compareTo(TimelessDateValue o) {
        if (o == null || o.isEmpty()) {
            return this.isEmpty() ? 0 : 1;
        } else {
            return this.isEmpty() ? -1 : value.compareTo(o.value);
        }
    }

    @Override
    public final TimelessDateValue getPlatformClone() {
        final TimelessDate td = get();
        if (this.getClass() != TimelessDateValue.class) {
            return td == null ? new TimelessDateValue() : new TimelessDateValue(new TimelessDate(td.getYear(), td.getMonth(), td.getDayOfMonth()));
        } else if (td != null && td.getClass() != TimelessDate.class) {
            return new TimelessDateValue(new TimelessDate(td.getYear(), td.getMonth(), td.getDayOfMonth()));
        } else {
            return this;
        }
    }

    public TimelessDate getValue() {
        return value;
    }
}