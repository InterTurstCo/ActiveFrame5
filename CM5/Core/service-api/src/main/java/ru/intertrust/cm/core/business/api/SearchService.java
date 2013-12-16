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
     * 
     * @param query Строка для поиска
     * @param areaName Имя области поиска
     * @param objectType Тип искомых доменных объектов
     * @param maxResults Ограничение количества найденных объектов
     * @return Коллекция найденных доменных объектов
     */
    IdentifiableObjectCollection search(String query, String areaName, String objectType, int maxResults);

    /**
     * Выполняет многокритериальный по полям доменных объектов и вложениям.
     * 
     * @param query Критерии поиска
     * @param maxResults Ограничение количества найденных объектов
     * @return Коллекция найденных доменных объектов
     */
    IdentifiableObjectCollection search(SearchQuery query, int maxResults);
}
