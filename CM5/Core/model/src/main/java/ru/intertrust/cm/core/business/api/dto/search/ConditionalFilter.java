package ru.intertrust.cm.core.business.api.dto.search;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import ru.intertrust.cm.core.business.api.dto.SearchFilterBase;

/**
 * Универсальный фильтр, способный работать с любыми типами объектов и условий<br/>
 * Подклассы реализуют работу с различными разновидностями операций над этими
 * объектами: простыми, операциями над множествами и над интервалами.
 *
 * @param <T>
 */
public abstract class ConditionalFilter<T> extends SearchFilterBase {

    public static final String OPTION_DELIM = "#";

    /**
     * Условие для параметра поиска
     *
     * @author VMorozov
     *
     */
    enum Condition {

        /**
         * значение непусто: для логич. полей - значение поля истинно, для строк - непустая строка, для коллекций -
         * непустая коллекция, для чисел - не 0; если передан класс, то проверяет наличие/отсутствие дочерних сущностей этого класса
         */
        EXISTS,

        /**
         * значение эквивалентно; считаем, что: null == пустая коллекция, null == Boolean.FALSE, null == пустая строка,
         * null == 0.0, коллекция с 1 элементом == этому элементу, но между собой всегда не-null != не-null для разных
         * типов (т.е. 0.0 != Boolean.FALSE) (но лучше использовать конкретное пустое значение нужного типа)
         */
        EQUALS,

        /**
         * множество-параметр пересекается со значением в поле (может использоваться также, если однозначное поле должно
         * иметь значение из множества); считаем, что две пустые коллекции пересекаются
         */
        INTERSECT,

        /**
         * значение в поле (одно- или многозначное) полностью входит в параметр поиска (однозначный, коллекцию или интервал);
         * считаем, что пустые множества входят друг в друга
         */
        V_SUBSET,

        /**
         * параметр (однозначный или коллекция) полностью входит в значение в поле (одно- или многозначное); считаем,
         * что пустые множества входят друг в друга
         */
        P_SUBSET,

        /**
         * используется только для строк, обозначает регистронезависимый RegEx; если значение в поле пусто или не
         * строка, то вернёт false; если параметр пуст или не строка, то выкинет {@link IllegalArgumentException}
         */
        MATCH,

        /**
         * "Меньше" (<). Используется для сравнения чисел, дат, строк и пр.
         */
        LT,

        /**
         * Включающее "меньше" (<=). Используется для сравнения чисел, дат, строк и пр.
         */
        LT_INC,

        /**
         * "Больше" (>). Используется для сравнения чисел, дат, строк и пр.
         */
        GT,

        /**
         * Включающее "больше" (>=). Используется для сравнения чисел, дат, строк и пр.
         */
        GT_INC,

        /**
         * Операция бинарного включения (только для целых чисел): все биты включаемого содержатся во включающем числе
         *
         * Аналог формулы: (paramInt & valInt) >= paramInt
         */
        BINARY_IN,

        /**
         * Операция бинарного пересечения (только для целых чисел): хотя бы один бит содержится и в первом, и во втором числах
         *
         * Аналог формулы: (paramInt & valInt) > 0
         */
        BINARY_INTERSECT
    }

    private final T value;

    private final Condition condition;

    private final String options;

    protected ConditionalFilter(@Nonnull String field, @Nonnull Condition c, T value) {
        super(!field.contains(OPTION_DELIM) ? field : field.substring(0, field.indexOf(OPTION_DELIM)));
        if (!allowedConditions().contains(c)) {
            throw new IllegalArgumentException("Condition doesn't supported by this filter: " + c);
        }
        this.condition = c;
        this.value = value;
        this.options = field.contains(OPTION_DELIM) ? field.substring(field.indexOf(OPTION_DELIM)) : "";
    }

    public Condition getCondition() {
        return condition;
    }

    public T getValue() {
        return value;
    }

    public String getOptions() {
        return options;
    }

    public abstract EnumSet<Condition> allowedConditions();
}
