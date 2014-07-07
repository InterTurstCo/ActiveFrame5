package ru.intertrust.cm.core.business.api.dto.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZone;
import ru.intertrust.cm.core.business.api.dto.DateTimeWithTimeZoneValue;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.TimelessDate;
import ru.intertrust.cm.core.business.api.dto.TimelessDateValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

/**
 * Хранение списка однотипных значений для передачи в SQL in () выражение. Не отображается на поля доменного объекта!
 * Используется для удобства передачи параметров при поиске коллекций (@see
 * {@link ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl#findCollectionByQuery(String, List, int, int, ru.intertrust.cm.core.dao.access.AccessToken)}
 * )
 * @author atsvetkov
 */
public class ListValue extends Value<ListValue> {

    private List<Serializable> values;

    public ListValue() {
        this.values = new ArrayList<>();
    }

    /**
     * Преобразует список @link(Value) в список типов, поддерживаемых in() выражением
     * @param values целочичсленное значение
     */
    public ListValue(List<Value> values) {
        this.values = getPrimitiveValues(values);
    }

    @Override
    public List<Serializable> get() {
        return values;
    }

    private List<Serializable> getPrimitiveValues(List<Value> values) {
        if (values == null || values.size() <= 0) {
            return null;
        }
        List<Serializable> resultValues = new ArrayList<>();
        for (Value value : values) {
            if (value instanceof ReferenceValue) {
                // передаются long-идентификаторы для ссылочных полей. Для общих ссылок (звездочка) нужна доработка.
                resultValues.add(((RdbmsId) value.get()).getId());
            } else if (value instanceof DateTimeWithTimeZoneValue) {
                resultValues.add(getDateFromDateTimeWithTimezoneValue((DateTimeWithTimeZone) value.get()));
            } else if (value instanceof TimelessDateValue) {
                resultValues.add(getDateFromTimelessDateValue((TimelessDate) value.get()));
            }
            else {
                resultValues.add((Serializable) value.get());
            }
        }
        return resultValues;
    }

    public static Date getDateFromDateTimeWithTimezoneValue(final DateTimeWithTimeZone dateTime) {
        final Calendar calendar =
                Calendar.getInstance(TimeZone.getTimeZone(dateTime.getTimeZoneContext().getTimeZoneId()));
        calendar.set(Calendar.YEAR, dateTime.getYear());
        calendar.set(Calendar.MONTH, dateTime.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, dateTime.getDayOfMonth());
        calendar.set(Calendar.HOUR, dateTime.getHours());
        calendar.set(Calendar.MINUTE, dateTime.getMinutes());
        calendar.set(Calendar.SECOND, dateTime.getSeconds());
        calendar.set(Calendar.MILLISECOND, dateTime.getMilliseconds());
        return calendar.getTime();
    }

    public static Date getDateFromTimelessDateValue(final TimelessDate date) {
        final Calendar calendar =
                Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
        return calendar.getTime();
    }

}
