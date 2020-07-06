package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.SearchFilter;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

/**
 * Расширенный вариант {@link FilterAdapter}, позволяющий обрабатывать составные фильтры,
 * т.е. фильтры, включающие несколько разных полей.
 * При обработке поисковых запросов для каждого входящего в них фильтра {@link SearchServiceImpl}
 * предварительно вызывает метод {@link #isCompositeFilter(SearchFilter)}, чтобы определить, является ли данный фильтр
 * составным. Для простых фильтров затем вызывается метод {{@link #getFilterString(SearchFilter, SearchQuery)},
 * а для составных &mdash; определённый в данном интефейсе
 * {{@link #processCompositeFilter(SearchFilter, SearchServiceImpl.QueryProcessor, SearchQuery)}.
 * 
 * @author apirozhkov
 *
 * @param <F> Обрабатываемый тип фильтра
 */
public interface CompositeFilterAdapter<F extends SearchFilter> extends FilterAdapter<F> {

    /**
     * Выполняет обработку сложных запросов, которые включают несколько различных полей.
     * Метод может добавлять фильтры в формируемый объект запроса либо создавать подзапросы.
     * Метод должен вернуть объект подзапроса, если он был создан, или null, если изменялся исходный запрос.
     * Вызывается только в том случае, если предварительный вызов {{@link #isCompositeFilter(SearchFilter)}
     * для данного фильтра вернул true.
     * @param filter Обрабатываемый фильтр
     * @param queryProcessor Формируемый объект запроса
     * @param query Исходный поисковый запрос
     * @return объект подзапроса или null
     */
    SearchServiceImpl.QueryProcessor processCompositeFilter(F filter,
            SearchServiceImpl.QueryProcessor queryProcessor, SearchQuery query);
}
