package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Поисковый фильтр, позволяющий соединить несколько других фильтров по условию "И" или "ИЛИ",
 * конструируя запросы произвольной сложности.
 * Если операция соединения фильтров не была задана явно, предполагается "ИЛИ" ({@link #OR}).
 * 
 * <p>Пример использования:
 * <code>
 *  SearchQuery query = new SearchQuery();
 *  query.addFilter(new CombiningFilter(CombiningFilter.OR,
 *      new TextSearchFilter(SearchFilter.EVERYWHERE, "searched words"),
 *      new NegativeFilter(new OneOfListFilter("status", someId, anotherId)),
 *      new CombiningFilter(CombiningFilter.AND,
 *          new DatePeriodFilter("regDate", dateStart, dateEnd),
 *          new DatePeriodFilter("execDate", dateEnd, null)
 *      )
 *  );
 * </code>
 * 
 * <p>Другой вариант конструирования комбинированного поискового фильтра, эквивалентного предыдущему:
 * <code>
 *  CombiningFilter filter = new CombiningFilter()
 *      .addFilter(new TextSearchFilter(SearchFilter.EVERYWHERE, "searched words"))
 *      .addFilter(new NegativeFilter(new OneOfListFilter("status", someId, anotherId))
 *      .addFilter(new CombiningFilter().setOperation(CombiningFilter.AND)
 *          .addFilter(new DatePeriodFilter("regDate", dateStart, dateEnd))
 *          .addFilter(new DatePeriodFilter("execDate", dateEnd, null))
 *      );
 * </code>
 * 
 * @author apirozhkov
 */
public class CombiningFilter implements SearchFilter {

    public static enum Op {
        OR, AND
    }

    /**
     * Соединение фильтров операцией "ИЛИ" (объединение)
     */
    public static final Op OR = Op.OR;
    /**
     * Соединение фильтров операцией "И" (пересечение)
     */
    public static final Op AND = Op.AND;

    private Op operation = OR;

    private List<SearchFilter> filters;

    /**
     * Создаёт пустой объект фильтра.
     */
    public CombiningFilter() {
        this.filters = new ArrayList<>();
    }

    /**
     * Создаёт пустой объект фильтра с заданной операцией соединения.
     * Соединяемые фильтры должны быть добавлены через операцию {@link #addFilter(SearchFilter)}.
     * @param operation операция соединения - {@link #OR} или {@link #AND}
     */
    public CombiningFilter(Op operation) {
        this.operation = operation;
        this.filters = new ArrayList<>();
    }

    /**
     * Создаёт комбинированный фильтр из нескольких заданных, соединяя их операцией "ИЛИ".
     * @param filters соединяемые фильтры
     */
    public CombiningFilter(SearchFilter... filters) {
        this.filters = Arrays.asList(filters);
    }

    /**
     * Создаёт комбинированный фильтр из нескольких заданных с заданной операцией соединения.
     * @param operation операция соединения - {@link #OR} или {@link #AND}
     * @param filters соединяемые фильтры
     */
    public CombiningFilter(Op operation, SearchFilter... filters) {
        this.operation = operation;
        this.filters = Arrays.asList(filters);
    }

    /**
     * Создаёт комбинированный фильтр из нескольких заданных, соединяя их операцией "ИЛИ".
     * @param filters коллекция соединяемых фильтров
     */
    public CombiningFilter(Collection<? extends SearchFilter> filters) {
        this.filters.addAll(filters);
    }

    /**
     * Создаёт комбинированный фильтр из нескольких заданных с заданной операцией соединения.
     * @param operation операция соединения - {@link #OR} или {@link #AND}
     * @param filters коллекция соединяемых фильтров
     */
    public CombiningFilter(Op operation, Collection<? extends SearchFilter> filters) {
        this.operation = operation;
        this.filters.addAll(filters);
    }

    /**
     * Добавляет комбинируемый фильтр к списку.
     * Метод возвращает текущий объект CombiningFilter, чтобы вызовы можно было соединять в цепочку.
     * @param filter добавляемый фильтр
     * @return this
     */
    public CombiningFilter addFilter(SearchFilter filter) {
        this.filters.add(filter);
        return this;
    }

    /**
     * Задаёт операцию соединения вложенных фильтров.
     * Метод возвращает текущий объект CombiningFilter, чтобы вызовы можно было соединять в цепочку.
     * @param operation операция соединения - {@link #OR} или {@link #AND}
     * @return this
     */
    public CombiningFilter setOperation(Op operation) {
        this.operation = operation;
        return this;
    }

    @Override
    public String getFieldName() {
        if (filters.size() == 1) {
            return filters.get(0).getFieldName();
        }
        return new StringBuilder("comb$").append(Integer.toHexString(hashCode())).toString();
    }

    /**
     * Возвращает операцию соединения фильтров
     * @return операция соединения
     */
    public Op getOperation() {
        return operation;
    }

    /**
     * Возвращает список вложенных фильтров.
     * Если вложенные фильтры не были заданы, возвращается пустой список.
     * @return список фильтров
     */
    public List<SearchFilter> getFilters() {
        return filters;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (SearchFilter filter : filters) {
            if (result.length() > 0) {
                result.append(" ").append(operation.name()).append(" ");
            }
            result.append(filter.toString());
        }
        result.insert(0, "(").append(")");
        return result.toString();
    }
}
