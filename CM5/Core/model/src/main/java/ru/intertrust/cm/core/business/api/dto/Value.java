package ru.intertrust.cm.core.business.api.dto;

import java.util.Comparator;

/**
 * Значение поля доменного объекта. По сути является отображением типов, определённых в системе на типы Java.
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:10
 */
public abstract class Value<T extends Value<T>> implements Dto, Comparable<T> {
	private static final long serialVersionUID = -6565138416908113065L;

	/**
     * Создаёт значение поля доменного объекта
     */
    public Value() {
    }

    /**
     * Возвращает значение поля доменного объекта, отображённое в Java-тип
     * @return значение поля доменного объекта, отображённое в Java-тип
     */
    public abstract Object get();

    /**
     * Проверяет значение на "пустоту"
     * @return true, если объект "пуст" и false - в противном случае
     */
    public boolean isEmpty() {
        return get() == null;
    }

    /**
     * Возвращает тип поля, значение которого определеятся данным объектом
     * @return enum-константа, определяющая тип поля
     */
    public final FieldType getFieldType() {
        return FieldType.find(getClass());
    }

    /**
     * Возвращает true, если состояние объекта невозможно изменить ничем, кроме механизмов рефлексии (Reflection). В противном случае возвращает false
     * @return true, если состояние объекта невозможно изменить ничем, кроме механизмов рефлексии (Reflection). В противном случае возвращает false
     */
    public boolean isImmutable() {
        return false;
    }

    @Override
    public int hashCode() {
        Object object = get();
        return object == null ? -1 : object.hashCode();
    }

    @Override
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }
        if (another == null || !(another instanceof Value)) {
            return false;
        }
        Object thisValue = get();
        Object anotherValue = ((Value) another).get();
        return thisValue == null && anotherValue == null || thisValue != null && thisValue.equals(anotherValue);
    }

    @Override
    public int compareTo(T o) {
        // GWT-хак (работает начиная с GWT 2.6). Этот метод в DateTimeWithTimeZoneValue помечен как несовместимый с GWT,
        // поэтому в его случае GWT исполнит данный базовый метод (с его точки зрения он окажется не переопределён). На
        // всякий случай, добавлена проверка совпадения классов (чтобы кто-то по ошибке не отнаследовался, забыв переопределить
        // этот метод.
        if (this.getClass() == DateTimeWithTimeZoneValue.class) {
            return ((DateTimeWithTimeZoneValue) (Value) this).gwtCompareTo((DateTimeWithTimeZoneValue) o);
        }
        return 0;
    }

    /**
     * Возвращает Java Comparator, позволяющий сравнить значения {@link Value}. Пустые (nulls) значения разрешены.
     * Исключение {@link ClassCastException} будет выброшено при попытке использовать Comparator для сравнения объектов разных типов.
     * @param asc true, если сортировка будет осуществляться по возрастанию, false - в противном случае
     * @param nullsFirstWhenSortedAsc true, если пустые значения должны идти вначале в случае сортировки по возрастанию, false - если в конце
     * @return Java Comparator, позволяющий сортировать объекты {@link Value}
     */
    public static Comparator<Value> getComparator(boolean asc, boolean nullsFirstWhenSortedAsc) {
        if (asc) {
            if (nullsFirstWhenSortedAsc) {
                return new Comparator<Value>() {
                    @Override
                    public int compare(Value o1, Value o2) {
                        return defaultAscCompare(o1, o2, -1, 1);
                    }
                };
            } else {
                return new Comparator<Value>() {
                    @Override
                    public int compare(Value o1, Value o2) {
                        return defaultAscCompare(o1, o2, 1, -1);
                    }
                };
            }
        } else {
            if (nullsFirstWhenSortedAsc) {
                return new Comparator<Value>() {
                    @Override
                    public int compare(Value o1, Value o2) {
                        return -defaultAscCompare(o1, o2, -1, 1);
                    }
                };
            } else {
                return new Comparator<Value>() {
                    @Override
                    public int compare(Value o1, Value o2) {
                        return -defaultAscCompare(o1, o2, 1, -1);
                    }
                };
            }
        }
    }

    private static int defaultAscCompare(final Value o1, final Value o2, final int o1NullResult, final int o2NullResult) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null || o1.isEmpty()) {
            return o1NullResult;
        }
        if (o2 == null || o2.isEmpty()) {
            return o2NullResult;
        }
        return o1.compareTo(o2);
    }

    @Override
    public String toString() {
        Object value = get();
        return value == null ? "null" : value.toString();
    }

    /**
     * Возвращает платформенный клон, который можно безопасно использовать при кэшировании. Под платформенным подразумевается тип-наследник {@link Value},
     * на который непеосредственно отображаются объекты хранилища, например {@link StringValue}. Объект платформенного типа при попадании в кэш не приведёт к использованию
     * большего количества памяти, чем требуется. Превышение возможно при использовании наследников системных {@link Value}, которые могут занимать произвольный объём памяти.
     * Чаще всего, опасность наличия неконтролируемого количества ссылок на другие объекты системы, которые не являются частью системных Value,
     * возникает в анонимных классах-наследниках, неявно ссылающихся на объект внешнего класса.
     * Во избежание изменения поведения метода в наследниках, систменые классы-наследники {@link Value} определяют данный метод как final.
     * @return клон системного объекта-значения.
     */
    public T getPlatformClone() {
        return null;
    }
}
