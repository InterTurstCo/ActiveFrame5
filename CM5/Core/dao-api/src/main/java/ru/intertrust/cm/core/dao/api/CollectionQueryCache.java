package ru.intertrust.cm.core.dao.api;

import java.util.List;
import java.util.Set;

import ru.intertrust.cm.core.business.api.FilterForCache;
import ru.intertrust.cm.core.business.api.QueryModifierPrompt;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.dao.access.AccessToken;

/**
 * Глобальный кеш SQL запросов коллекций. Так как запросы для коллекций в
 * системе не меняются, то имеет смысл их кешировать, особенно для уменьшения
 * кол-ва распаршивания запросов (используя JSQLParser).
 * @author atsvetkov
 */
public interface CollectionQueryCache {

    /**
     * Очистка кеша запросов.
     */
    void clearCollectionQueryCache();

    /**
     * Получение SQL запроса для размера коллекции из кеша. В качестве ключа
     * кеширования используются параметры соответвующего метода
     * {@link CollectionsDao#findCollectionCount(String, List, AccessToken)}
     * @param collectionName
     * @param filterValues
     * @param queryModifierPrompt
     * @param accessToken
     * @return
     */
    CollectionQueryEntry getCollectionCountQuery(String collectionName, Set<FilterForCache> filterValues, QueryModifierPrompt queryModifierPrompt,
            AccessToken accessToken);

    Set<String> getCollectionDomainObjectTypes(String query);

    Set<String> getCollectionDomainObjectTypes(String collectionName, Set<String> filterNames);

    /**
     * Получение SQL запроса коллекции из кеша. Используется для кеширования
     * неконфигурируемых коллекций (найденных через
     * {@link CollectionsDao#findCollectionByQuery(String, List, int, int, AccessToken)}
     * ). В качестве ключа кеширования используются параметры соответвующего
     * метода
     * {@link CollectionsDao#findCollectionByQuery(String, int, int, AccessToken)}
     * @param collectionQuery
     * @param offset
     * @param limit
     * @param prompt
     *            TODO
     * @param accessToken
     * @return
     */
    CollectionQueryEntry getCollectionQuery(String collectionQuery, int offset, int limit, QueryModifierPrompt prompt, AccessToken accessToken);

    /**
     * Получение SQL запроса коллекции из кеша. В качестве ключа кеширования
     * используются параметры соответвующего метода
     * {@link CollectionsDao#findCollection(String, List, SortOrder, int, int, AccessToken)}
     * @param collectionName
     * @param filtersForCache
     * @param prompt
     *            TODO
     * @param sortOrder
     * @param offset
     * @param limit
     * @param accessToken
     * @return
     */
    CollectionQueryEntry getCollectionQuery(String collectionName, Set<FilterForCache> filtersForCache, QueryModifierPrompt prompt, SortOrder sortOrder,
            int offset,
            int limit, AccessToken accessToken);

    /**
     * Помещение SQL запроса для размера коллекции в кеш.
     * @param collectionName
     * @param filterValues
     * @param queryModifierPrompt
     * @param accessToken
     * @param queryEntry
     */
    void putCollectionCountQuery(String collectionName, Set<FilterForCache> filterValues, QueryModifierPrompt queryModifierPrompt, AccessToken accessToken,
            CollectionQueryEntry queryEntry);

    void putCollectionDomainObjectTypes(String query, Set<String> types);

    void putCollectionDomainObjectTypes(String collectionName, Set<String> filterNames, Set<String> types);

    /**
     * Помещение SQL запроса коллекции в кеш. Используется для кеширования
     * неконфигурируемых коллекций (найденных через
     * {@link CollectionsDao#findCollectionByQuery(String, List, int, int, AccessToken)}
     * ).
     * @param collectionQuery
     * @param offset
     * @param limit
     * @param prompt
     *            TODO
     * @param accessToken
     * @param queryEntry
     */
    void putCollectionQuery(String collectionQuery, int offset, int limit, QueryModifierPrompt prompt, AccessToken accessToken,
            CollectionQueryEntry queryEntry);

    /**
     * Помещение SQL запроса коллекции в кеш. В качестве ключа кеширования
     * используются параметры соответвующего метода
     * {@link CollectionsDao#findCollection(String, List, SortOrder, int, int, AccessToken)}
     * @param collectionName
     * @param filterValues
     * @param prompt
     *            TODO
     * @param sortOrder
     * @param offset
     * @param limit
     * @param accessToken
     * @param queryEntry
     */
    void putCollectionQuery(String collectionName, Set<FilterForCache> filterValues, QueryModifierPrompt prompt, SortOrder sortOrder, int offset, int limit,
            AccessToken accessToken, CollectionQueryEntry queryEntry);

}
