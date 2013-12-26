package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.List;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:40 PM
 */
public interface CollectionsService {
    public interface Remote extends CollectionsService {
    }

    /**
     * Возвращает заданную коллекцию, отфильтрованную и упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @param limit          максимальное количество возвращаемых доменных объектов
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, List<Filter> filters, int offset, int limit);

    /**
     * Возвращает заданную коллекцию, отфильтрованную и упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder, List<Filter> filters);

    /**
     * Возвращает заданную коллекцию, упорядоченную согласно порядку сортировки
     *
     * @param collectionName название коллекции
     * @param sortOrder      порядок сортировки коллекции {@link ru.intertrust.cm.core.business.api.dto.SortOrder}
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollection(String collectionName, SortOrder sortOrder);

    /**
     * Возвращает заданную коллекцию
     *
     * @param collectionName название коллекции
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollection(String collectionName);

    /**
     * Возвращает коллекцию по запросу
     * @param query запрос
     * @param limit максимальное количество возвращаемых доменных объектов
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollectionByQuery(String query, int offset, int limit);


    /**
     * Возвращает коллекцию по запросу
     * @param query запрос
     * @return коллекцию объектов {@link ru.intertrust.cm.core.business.api.dto.IdentifiableObject}
     */
    IdentifiableObjectCollection findCollectionByQuery(String query);

    /**
     * Возвращает количество элементов заданной коллекции, отфильтрованной согласно списку фильтров
     *
     * @param collectionName название коллекции
     * @param filters        список фильтров {@link ru.intertrust.cm.core.business.api.dto.Filter}
     * @return число элементов заданной коллекции
     */
    int findCollectionCount(String collectionName, List<Filter> filters);

    /**
     * Поиск коллекции доменных объектов, используя запрос с переданными параметрами. Используются нумерованные параметры вида {0}, {1} и т.д.
     * При этом переданные параметры должны идти в том же порядке (в List<Value> params), в котором указаны их индексы в SQL запросе.
     * @param query SQL запрос
     * @param params параметры запроса
     * @param offset смещение
     * @param limit ограничение количества возвращенных доменных объектов
     * @param accessToken маркер доступа
     * @return результат поиска в виде {@link IdentifiableObjectCollection}
     */
    IdentifiableObjectCollection findCollectionByQuery(String query, List<Value> params, int offset, int limit);    

    /**
     * Поиск коллекции доменных объектов, используя запрос с переданными параметрами.
     * @param query SQL запрос
     * @param params параметры запроса
     * @return результат поиска в виде {@link IdentifiableObjectCollection}
     */
    IdentifiableObjectCollection findCollectionByQuery(String query, List<Value> params);    

}
