package ru.intertrust.cm.core.dao.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.FilterForCache;
import ru.intertrust.cm.core.business.api.QueryModifierPrompt;
import ru.intertrust.cm.core.business.api.dto.LruLimitedSynchronizedMap;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.CollectionQueryCacheConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionQueryCache;
import ru.intertrust.cm.core.dao.api.CollectionQueryEntry;
import ru.intertrust.cm.core.dao.dto.CollectionTypesKey;
import ru.intertrust.cm.core.dao.dto.NamedCollectionTypesKey;
import ru.intertrust.cm.core.dao.dto.QueryCollectionTypesKey;

/**
 * 
 * @author atsvetkov
 * 
 */
public class CollectionQueryCacheImpl implements CollectionQueryCache {

    public static Map<CollectionQueryKey, CollectionQueryEntry> collectionQueryCache = new ConcurrentHashMap<>();
    private static Map<CollectionTypesKey, Set<String>> collectionDomainObjectTypes = new LruLimitedSynchronizedMap<>(10000);
    private static final int MAX_QUERY_SIZE_TO_CACHE = 5000;

    private static final Logger logger = LoggerFactory.getLogger(CollectionQueryCacheImpl.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    private CollectionQueryLogTimer collectionQueryLogTimer;

    public static class CollectionQueryLogTimer {
        private static final int LOG_TIME_INTERVAL = 10000;
        private Long startTime;

        public CollectionQueryLogTimer() {
            startTime = System.currentTimeMillis();
        }

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public boolean canWrite() {

            Long checkTime = System.currentTimeMillis();
            if (checkTime - startTime > LOG_TIME_INTERVAL) {
                startTime = checkTime;
                return true;
            }
            return false;
        }
    }

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void init() {
        collectionQueryLogTimer = new CollectionQueryLogTimer();
    }

    /**
     * Ключ для идентификации SQL запроса коллекции в кеше. Состоит из
     * параметров запроса.
     * @author atsvetkov
     */
    public static class CollectionQueryKey {
        public static final int MAX_PARAMS_TO_CACHE = 50;

        private String collectionNameOrQuery;
        private Set<FilterForCache> filtersForCache = new HashSet<>();
        private SortOrder sortOrder;
        private Integer offset;
        private Integer limit;
        private AccessToken accessToken;
        private QueryModifierPrompt prompt;

        public CollectionQueryKey(String collectionNameOrQuery, Set<FilterForCache> filterValues, SortOrder sortOrder, Integer offset,
                Integer limit,
                QueryModifierPrompt prompt,
                AccessToken accessToken) {
            this.collectionNameOrQuery = collectionNameOrQuery;
            this.filtersForCache = filterValues == null ? new HashSet<FilterForCache>() : filterValues;

            this.sortOrder = sortOrder;
            this.offset = offset;
            this.limit = limit;
            this.accessToken = accessToken;
            this.prompt = prompt;
        }

        public boolean shouldBeCached() {
            int paramsQty = 0;
            for (FilterForCache filterForCache : filtersForCache) {
                paramsQty += filterForCache.getParamsCount();
            }
            if (prompt != null) {
                for (Integer quantity : prompt.getIdParamsPrompt().values()) {
                    paramsQty += quantity;
                }
            }
            return paramsQty < MAX_PARAMS_TO_CACHE;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((accessToken == null) ? 0 : (accessToken.isDeferred() ? 1 : 0));
            result = prime * result + ((collectionNameOrQuery == null) ? 0 : collectionNameOrQuery.hashCode());
            result = prime * result + ((filtersForCache == null) ? 0 : filtersForCache.hashCode());
            result = prime * result + ((prompt == null) ? 0 : prompt.hashCode());

            result = prime * result + ((limit == null) ? 0 : limit.hashCode());
            result = prime * result + ((offset == null) ? 0 : offset.hashCode());
            result = prime * result + ((sortOrder == null) ? 0 : sortOrder.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CollectionQueryKey other = (CollectionQueryKey) obj;

            if (!equalAccessTokens(accessToken, other.accessToken)) {
                return false;
            }

            if (collectionNameOrQuery == null) {
                if (other.collectionNameOrQuery != null) {
                    return false;
                }
            } else if (!collectionNameOrQuery.equals(other.collectionNameOrQuery)) {
                return false;
            }

            if (!filtersForCache.equals(other.filtersForCache)) {
                return false;
            }

            if (prompt == null) {
                if (other.prompt != null) {
                    return false;
                }
            } else if (!prompt.equals(other.prompt)) {
                return false;
            }

            if (limit == null) {
                if (other.limit != null) {
                    return false;
                }
            } else if (!limit.equals(other.limit)) {
                return false;
            }
            if (offset == null) {
                if (other.offset != null) {
                    return false;
                }
            } else if (!offset.equals(other.offset)) {
                return false;
            }
            if (sortOrder == null) {
                if (other.sortOrder != null) {
                    return false;
                }
            } else if (!sortOrder.equals(other.sortOrder)) {
                return false;
            }
            return true;
        }

        private boolean equalAccessTokens(AccessToken token1, AccessToken token2) {
            if (token1 == null) {
                if (token2 != null) {
                    return false;
                }
            } else if (token2 != null && token1.isDeferred() != token2.isDeferred()) {
                return false;
            }
            return true;
        }

    }

    @Override
    public CollectionQueryEntry getCollectionQuery(String collectionNameOrQuery, Set<FilterForCache> filtersForCache, QueryModifierPrompt prompt,
            SortOrder sortOrder,
            int offset,
            int limit, AccessToken accessToken) {
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filtersForCache, sortOrder, offset, limit, prompt, accessToken);
            if (key.shouldBeCached()) { // check in order to avoid expensive
                                        // equals/hash code operations
                return collectionQueryCache.get(key);
            }
        }
        return null;
    }

    private CollectionQueryEntry getFromCache(String collectionNameOrQuery, Set<FilterForCache> filterValues, QueryModifierPrompt prompt, SortOrder sortOrder,
            int offset, int limit, AccessToken accessToken) {
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, sortOrder, offset, limit, prompt, accessToken);
            if (key.shouldBeCached()) { // check in order to avoid expensive
                                        // equals/hash code operations
                return collectionQueryCache.get(key);
            }
        }
        return null;
    }

    @Override
    public void putCollectionQuery(String collectionNameOrQuery, Set<FilterForCache> filterValues, QueryModifierPrompt prompt, SortOrder sortOrder, int offset,
            int limit, AccessToken accessToken, CollectionQueryEntry queryEntry) {
        putInCache(collectionNameOrQuery, filterValues, null, sortOrder, offset, limit, accessToken, queryEntry);
    }

    private void putInCache(String collectionNameOrQuery, Set<FilterForCache> filterValues, QueryModifierPrompt prompt, SortOrder sortOrder, int offset,
            int limit,
            AccessToken accessToken, CollectionQueryEntry queryEntry) {
        if (collectionQueryCache.size() > getCacheMaxSize()) {
            removeOneEntryFromCache();
            writeLog();
        }
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, sortOrder, offset, limit, prompt, accessToken);
            if (key.shouldBeCached() && queryEntry.getQuery().length() < MAX_QUERY_SIZE_TO_CACHE) {
                collectionQueryCache.put(key, queryEntry);
            } else {
                logger.debug("Query not cached as it's huge: " + queryEntry.getQuery());
            }
        }
    }

    private void removeOneEntryFromCache() {
        if (!collectionQueryCache.keySet().isEmpty()) {
            CollectionQueryKey key = collectionQueryCache.keySet().iterator().next();
            collectionQueryCache.remove(key);
        }
    }

    private void writeLog() {
        if (isAllowedToWriteLog()) {
            logger.debug("Collection query cache exceeds allowed cache size: " + getCacheMaxSize() + " records. One random entry was removed.");
        }
    }

    private boolean isAllowedToWriteLog() {
        return getCacheMaxSize() > 0 && collectionQueryLogTimer.canWrite();
    }

    @Override
    public CollectionQueryEntry getCollectionCountQuery(String collectionNameOrQuery, Set<FilterForCache> filterValues, QueryModifierPrompt prompt,
            AccessToken accessToken) {
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, null, null, null, prompt, accessToken);
            return collectionQueryCache.get(key);
        }
        return null;
    }

    @Override
    public void putCollectionCountQuery(String collectionNameOrQuery, Set<FilterForCache> filterValues, QueryModifierPrompt prompt, AccessToken accessToken,
            CollectionQueryEntry queryEntry) {
        if (collectionQueryCache.size() > getCacheMaxSize()) {
            writeLog();
            removeOneEntryFromCache();
        }
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, null, null, null, prompt, accessToken);
            collectionQueryCache.put(key, queryEntry);
        }
    }

    @Override
    public Set<String> getCollectionDomainObjectTypes(String collectionName, Set<String> filterNames) {
        return collectionDomainObjectTypes.get(new NamedCollectionTypesKey(collectionName, filterNames));
    }

    @Override
    public void putCollectionDomainObjectTypes(String collectionName, Set<String> filterNames, Set<String> types) {
        collectionDomainObjectTypes.put(new NamedCollectionTypesKey(collectionName, filterNames), Collections.unmodifiableSet(types));
    }

    @Override
    public Set<String> getCollectionDomainObjectTypes(String query) {
        return collectionDomainObjectTypes.get(new QueryCollectionTypesKey(query));
    }

    @Override
    public void putCollectionDomainObjectTypes(String query, Set<String> types) {
        collectionDomainObjectTypes.put(new QueryCollectionTypesKey(query), Collections.unmodifiableSet(types));
    }

    public Integer getCacheMaxSize() {
        CollectionQueryCacheConfig collectionQueryCache = configurationExplorer.getGlobalSettings().getCollectionQueryCacheConfig();
        if (collectionQueryCache != null) {
            return collectionQueryCache.getMaxSize();
        } else {
            return 1000;
        }
    }

    @Override
    public CollectionQueryEntry getCollectionQuery(String collectionQuery, int offset, int limit, QueryModifierPrompt prompt, AccessToken accessToken) {
        return getFromCache(collectionQuery, null, prompt, null, offset, limit, accessToken);
    }

    @Override
    public void putCollectionQuery(String collectionQuery, int offset, int limit, QueryModifierPrompt prompt, AccessToken accessToken,
            CollectionQueryEntry queryEntry) {
        putInCache(collectionQuery, null, prompt, null, offset, limit, accessToken, queryEntry);
    }

    @Override
    public void clearCollectionQueryCache() {
        collectionQueryCache.clear();
        collectionDomainObjectTypes.clear();
    }

}
