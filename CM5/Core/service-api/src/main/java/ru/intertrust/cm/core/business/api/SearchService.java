package ru.intertrust.cm.core.business.api;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;
import ru.intertrust.cm.core.business.api.dto.Value;

/**
 * Сервис поиска по содержимому доменных объектов и вложений.
 * 
 * @author apirozhkov
 */
public interface SearchService {

    public interface Remote extends SearchService {
    }

    /**
     * Выполняет поиск по всем полям доменных объектов и вложениям в одной области поиска.
     * Тип возвращаемых доменных объектов определяется конфигурацией запрошенной коллекции.
     * 
     * @param query Строка для поиска
     * @param areaName Имя области поиска
     * @param targetCollectionName Имя сконфигурированной коллекции для возвращаемых объектов
     * @param maxResults Ограничение количества найденных объектов
     * @return Коллекция найденных доменных объектов
     * @throws IllegalArgumentException если область поиска или коллекция отсутствуют в конфигурации,
     *      или если возвращаемый коллекцией тип доменных объектов не содержится в заданной области поиска
     */
    IdentifiableObjectCollection search(String query, String areaName, String targetCollectionName, int maxResults);

    /**
     * Выполняет многокритериальный по полям доменных объектов и вложениям в одной или нескольких областях поиска.
     * 
     * @param query Критерии поиска
     * @param targetCollectionName Имя сконфигурированной коллекции для возвращаемых объектов
     * @param maxResults Ограничение количества найденных объектов
     * @return Коллекция найденных доменных объектов
     * @throws IllegalArgumentException если критерии поиска содержат области поиска или типы искомых объектов,
     *      не объявленные в конфигурации, либо если коллекция-получатель не определена в конфигурации
     */
    IdentifiableObjectCollection search(SearchQuery query, String targetCollectionName, int maxResults);

    /**
     * Выполняет многокритериальный по полям доменных объектов и вложениям в одной или нескольких областях поиска.
     * 
     * @param query Критерии поиска
     * @param targetCollectionName Имя сконфигурированной коллекции для возвращаемых объектов
     * @param collectionFilters Фильтры, применяемые к возвращаемой коллекции
     * @param maxResults Ограничение количества найденных объектов
     * @return Коллекция найденных доменных объектов
     * @throws IllegalArgumentException если критерии поиска содержат области поиска или типы искомых объектов,
     *      не объявленные в конфигурации, либо если коллекция-получатель не определена в конфигурации
     */
    IdentifiableObjectCollection search(SearchQuery query, String targetCollectionName,
            List<? extends Filter> collectionFilters, int maxResults);

    /**
     * Выполняет многокритериальный по полям доменных объектов и вложениям в одной или нескольких областях поиска.
     * 
     * @param searchQuery Критерии поиска
     * @param sqlQuery SQL-запрос для выборки найденных объектов
     * @param maxResults Ограничение количества найденных объектов
     * @return Коллекция найденных доменных объектов
     * @throws IllegalArgumentException если критерии поиска содержат области поиска или типы искомых объектов,
     *      не объявленные в конфигурации
     */
    IdentifiableObjectCollection searchAndQuery(SearchQuery searchQuery, String sqlQuery, int maxResults);

    /**
     * Выполняет многокритериальный по полям доменных объектов и вложениям в одной или нескольких областях поиска.
     * 
     * @param searchQuery Критерии поиска
     * @param sqlQuery SQL-запрос для выборки найденных объектов
     * @param sqlParams параметры SQL-запроса
     * @param maxResults Ограничение количества найденных объектов
     * @return Коллекция найденных доменных объектов
     * @throws IllegalArgumentException если критерии поиска содержат области поиска или типы искомых объектов,
     *      не объявленные в конфигурации
     */
    IdentifiableObjectCollection searchAndQuery(SearchQuery searchQuery,
            String sqlQuery, List<? extends Value<?>> sqlParams, int maxResults);

    /**
     * Выполняет поиск по вложениям и полям доменных объектов одной области поиска.
     *
     * @param searchQuery Критерии поиска
     * @param maxResults Ограничение количества найденных объектов
     * @return Коллекция найденных доменных объектов
     * @throws IllegalArgumentException если критерии поиска содержат области поиска или типы искомых объектов,
     *      не объявленные в конфигурации
     */
    IdentifiableObjectCollection search(SearchQuery searchQuery, int maxResults);

    void dumpAll();
}
