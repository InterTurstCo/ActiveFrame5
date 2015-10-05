package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.LruLimitedSynchronizedMap;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.CollectionQueryCacheConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionQueryCache;
import ru.intertrust.cm.core.dao.api.CollectionQueryEntry;
import ru.intertrust.cm.core.dao.dto.CollectionTypesKey;
import ru.intertrust.cm.core.dao.dto.NamedCollectionTypesKey;
import ru.intertrust.cm.core.dao.dto.QueryCollectionTypesKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author atsvetkov
 *
 */
public class CollectionQueryCacheImpl implements CollectionQueryCache {


    public static Map<CollectionQueryKey, CollectionQueryEntry> collectionQueryCache = new ConcurrentHashMap<>();
    private static Map<CollectionTypesKey, Set<String>> collectionDomainObjectTypes = new LruLimitedSynchronizedMap<>(10000);

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
     * Ключ для идентификации SQL запроса коллекции в кеше. Состоит из параметров запроса.
     * @author atsvetkov
     */
    public static class CollectionQueryKey {
        private String collectionNameOrQuery;
        private Set<FilterForCache> filtersForCache = new HashSet<>();
        private SortOrder sortOrder;
        private Integer offset;
        private Integer limit;
        private AccessToken accessToken;
        private Set<ListValue> listValueParams;

        public CollectionQueryKey(String collectionNameOrQuery, List<? extends Filter> filterValues, SortOrder sortOrder, Integer offset, Integer limit,
                Set<ListValue> listValueParams,
                AccessToken accessToken) {
            this.collectionNameOrQuery = collectionNameOrQuery;
            if (filterValues != null) {
                for (Filter filter : filterValues) {
                    this.filtersForCache.add(new FilterForCache(filter));
                }
            }
            this.sortOrder = sortOrder;
            this.offset = offset;
            this.limit = limit;
            this.accessToken = accessToken;
            this.listValueParams = listValueParams;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((accessToken == null) ? 0 : (accessToken.isDeferred() ? 1 : 0));
            result = prime * result + ((collectionNameOrQuery == null) ? 0 : collectionNameOrQuery.hashCode());
            result = prime * result + ((filtersForCache == null) ? 0 : filtersForCache.hashCode());
            result = prime * result + ((listValueParams == null) ? 0 : listValueParams.hashCode());
            
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

            if (listValueParams == null) {
                if (other.listValueParams != null) {
                    return false;
                }
            } else if (!listValueParams.equals(other.listValueParams)) {
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
    
    /**
     * Обертка для {@link Filter} с переопределенным методом equals() для кеширования запросов по фильтрам. В методе
     * equals() учитываются только название фильтра и названия параметров.
     * @author atsvetkov
     */
    private static class FilterForCache extends Filter {
        
        public FilterForCache(Filter filter) {
            this.filter = filter.getFilter();
            this.parameterMap = filter.getParameterMap();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Filter another = (Filter) o;

            if (filter == null) {
                if (another.getFilter() != null) {
                    return false;
                }
            } else if (!filter.equals(another.getFilter())) {
                return false;
            }

            Set paramNames = parameterMap != null ? parameterMap.keySet() : null;
            Set anotherParamNames = another.getParameterMap() != null ? another.getParameterMap().keySet() : null;

            if (paramNames == null) {
                if (anotherParamNames != null) {
                    return false;
                }
            } else if (!paramNames.equals(anotherParamNames)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = 31 + ((filter == null) ? 0 : filter.hashCode());
            Set<Integer> paramNames = parameterMap != null ? parameterMap.keySet() : null;
            if (paramNames != null) {
                result = 31 * result + paramNames.hashCode();
            }
            return result;
        }
    }
    
    @Override
    public CollectionQueryEntry getCollectionQuery(String collectionNameOrQuery, List<? extends Filter> filterValues, SortOrder sortOrder, int offset,
            int limit,
            AccessToken accessToken) {
        return getCollectionQuery(collectionNameOrQuery, filterValues, sortOrder, offset, limit, null, accessToken);
    }

    private CollectionQueryEntry getCollectionQuery(String collectionNameOrQuery, List<? extends Filter> filterValues, SortOrder sortOrder, int offset,
            int limit, Set<ListValue> listValueParams, AccessToken accessToken) {
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, sortOrder, offset, limit, listValueParams, accessToken);
            return collectionQueryCache.get(key);
        }
        return null;
    }

    @Override
    public void putCollectionQuery(String collectionNameOrQuery, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,
            AccessToken accessToken, CollectionQueryEntry queryEntry) {
        putCollectionQuery(collectionNameOrQuery, filterValues, sortOrder, offset, limit, null, accessToken, queryEntry);
    }

    private void putCollectionQuery(String collectionNameOrQuery, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,  Set<ListValue> listValueParams,
            AccessToken accessToken, CollectionQueryEntry queryEntry) {
        if (collectionQueryCache.size() > getCacheMaxSize()) {
            removeOneEntryFromCache();
            writeLog();
        }
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, sortOrder, offset, limit, listValueParams, accessToken);
            collectionQueryCache.put(key, queryEntry);
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
            logger.warn("Collection query cache exceeds allowed cache size: " + getCacheMaxSize() + " records. One random entry was removed.");
        }
    }

    private boolean isAllowedToWriteLog() {
        return getCacheMaxSize() > 0 && collectionQueryLogTimer.canWrite();
    }

    @Override
    public CollectionQueryEntry getCollectionCountQuery(String collectionNameOrQuery, List<? extends Filter> filterValues, AccessToken accessToken) {
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, null, null, null, null, accessToken);
            return collectionQueryCache.get(key);
        }
        return null;
    }

    @Override
    public void putCollectionCountQuery(String collectionNameOrQuery, List<? extends Filter> filterValues, AccessToken accessToken, CollectionQueryEntry queryEntry) {
        if (collectionQueryCache.size() > getCacheMaxSize()) {
            writeLog();
            removeOneEntryFromCache();
        }
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, null, null, null, null, accessToken);
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
            return new Integer(0);
        }
    }
    
    @Override
    public CollectionQueryEntry getCollectionQuery(String collectionQuery, int offset, int limit, Set<ListValue> listValueParams, AccessToken accessToken) {
        return getCollectionQuery(collectionQuery, null, null, offset, limit, listValueParams, accessToken);
    }

    @Override
    public void putCollectionQuery(String collectionQuery, int offset, int limit, Set<ListValue> listValueParams, AccessToken accessToken, CollectionQueryEntry queryEntry) {
        putCollectionQuery(collectionQuery, null, null, offset, limit, listValueParams, accessToken, queryEntry);
    }
    
    @Override
    public void clearCollectionQueryCache() {
        collectionQueryCache.clear();
        collectionDomainObjectTypes.clear();
    }

}
