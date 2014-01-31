package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SearchQuery;

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
     */
    IdentifiableObjectCollection search(SearchQuery query, String targetCollectionName, int maxResults);

    void dumpAll();
}
