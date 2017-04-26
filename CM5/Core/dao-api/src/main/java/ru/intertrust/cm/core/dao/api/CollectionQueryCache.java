package ru.intertrust.cm.core.dao.api;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.dao.access.AccessToken;

import java.util.List;
import java.util.Set;

/**
 * Глобальный кеш SQL запросов коллекций. Так как запросы для коллекций в системе не меняются, то имеет смысл их
 * кешировать, особенно для уменьшения кол-ва распаршивания запросов (используя JSQLParser).
 * @author atsvetkov
 */
public interface CollectionQueryCache {

    /**
     * Получение SQL запроса коллекции из кеша. В качестве ключа кеширования используются параметры соответвующего
     * метода {@link CollectionsDao#findCollection(String, List, SortOrder, int, int, AccessToken)}
     * @param collectionName
     * @param filterValues
     * @param sortOrder
     * @param offset
     * @param limit
     * @param accessToken
     * @return
     */
    CollectionQueryEntry getCollectionQuery(String collectionName, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,
            AccessToken accessToken);

    /**
     * Получение SQL запроса коллекции из кеша. Используется для кеширования неконфигурируемых коллекций (найденных
     * через {@link CollectionsDao#findCollectionByQuery(String, List, int, int, AccessToken)}). В качестве ключа
     * кеширования используются параметры соответвующего метода
     * {@link CollectionsDao#findCollectionByQuery(String, int, int, AccessToken)}
     * @param collectionQuery
     * @param offset
     * @param limit
     * @param accessToken
     * @return
     */
    CollectionQueryEntry getCollectionQuery(String collectionQuery, int offset, int limit, Set<ListValue> listValueParams, AccessToken accessToken);

    /**
     * Помещение SQL запроса коллекции в кеш. Используется для кеширования неконфигурируемых коллекций (найденных через
     * {@link CollectionsDao#findCollectionByQuery(String, List, int, int, AccessToken)}).
     * @param collectionQuery
     * @param offset
     * @param limit
     * @param accessToken
     * @param queryEntry
     */
    void putCollectionQuery(String collectionQuery, int offset, int limit, Set<ListValue> listValueParams, AccessToken accessToken, CollectionQueryEntry queryEntry);

    /**
     * Помещение SQL запроса коллекции в кеш. В качестве ключа кеширования используются параметры соответвующего метода
     * {@link CollectionsDao#findCollection(String, List, SortOrder, int, int, AccessToken)}
     * @param collectionName
     * @param filterValues
     * @param sortOrder
     * @param offset
     * @param limit
     * @param accessToken
     * @param queryEntry
     */
    void putCollectionQuery(String collectionName, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit, AccessToken accessToken,
            CollectionQueryEntry queryEntry);

    /**
     * Получение SQL запроса для размера коллекции из кеша. В качестве ключа кеширования используются параметры
     * соответвующего метода {@link CollectionsDao#findCollectionCount(String, List, AccessToken)}
     * @param collectionName
     * @param filterValues
     * @param accessToken
     * @return
     */
    CollectionQueryEntry getCollectionCountQuery(String collectionName, List<? extends Filter> filterValues, AccessToken accessToken);

    /**
     * Помещение SQL запроса для размера коллекции в кеш.
     * @param collectionName
     * @param filterValues
     * @param accessToken
     * @param queryEntry
     */
    void putCollectionCountQuery(String collectionName, List<? extends Filter> filterValues, AccessToken accessToken, CollectionQueryEntry queryEntry);

    Set<String> getCollectionDomainObjectTypes(String collectionName, Set<String> filterNames);

    void putCollectionDomainObjectTypes(String collectionName, Set<String> filterNames, Set<String> types);

    Set<String> getCollectionDomainObjectTypes(String query);

    void putCollectionDomainObjectTypes(String query, Set<String> types);

    /**
     * Очистка кеша запросов.
     */
    void clearCollectionQueryCache();

}
