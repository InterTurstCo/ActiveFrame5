package ru.intertrust.cm.core.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.dto.Filter;
import ru.intertrust.cm.core.business.api.dto.SortOrder;
import ru.intertrust.cm.core.config.CollectionQueryCacheConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CollectionQueryCache;
import ru.intertrust.cm.core.dao.api.CollectionQueryEntry;

/**
 * 
 * @author atsvetkov
 *
 */
public class CollectionQueryCacheImpl implements CollectionQueryCache {


    public static Map<CollectionQueryKey, CollectionQueryEntry> collectionQueryCache = new ConcurrentHashMap <>();

    private static final Logger logger = LoggerFactory.getLogger(CollectionQueryCacheImpl.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Ключ для идентификации SQL запроса коллекции в кеше. Состоит из параметров запроса.
     * @author atsvetkov
     */
    public static class CollectionQueryKey {
        private String collectionNameOrQuery;
        private List<FilterForCache> filtersForCache = new ArrayList<>();
        private SortOrder sortOrder;
        private Integer offset;
        private Integer limit;
        private AccessToken accessToken;
        
        public CollectionQueryKey(String collectionNameOrQuery, List<? extends Filter> filterValues, SortOrder sortOrder, Integer offset, Integer limit,
                AccessToken accessToken) {
            this.collectionNameOrQuery = collectionNameOrQuery;
            if (filterValues != null) {
                for(Filter filter : filterValues){
                    this.filtersForCache.add(new FilterForCache(filter));
                }
            }
            this.sortOrder = sortOrder;
            this.offset = offset;
            this.limit = limit;
            this.accessToken = accessToken;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((accessToken == null) ? 0 : (accessToken.isDeferred() ? 1 : 0));
            result = prime * result + ((collectionNameOrQuery == null) ? 0 : collectionNameOrQuery.hashCode());
            result = prime * result + ((filtersForCache == null) ? 0 : filtersForCache.hashCode());
            
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

            if (!equalCollections(filtersForCache, other.filtersForCache)) {
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

        boolean equalCollections(List<? extends Filter> filters1, List<? extends Filter> filters2) {
            if (filters1 == null) {
                if (filters2 != null) {
                    return false;
                }
            } else if (!(filters1.size() == filters2.size() && filters1.containsAll(filters2))) {
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

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
        final int prime = 31;
            int result = 1;
            result = prime * result + ((filter == null) ? 0 : filter.hashCode());
            Set<Integer> paramNames = parameterMap != null ? parameterMap.keySet() : null;
            if (paramNames != null) {
                for (Integer param : paramNames) {
                    result = prime * result + ((param == null) ? 0 : param.hashCode());
                }
            }           
            
            return result;
        }

        
    }
    
    @Override
    public CollectionQueryEntry getCollectionQuery(String collectionNameOrQuery, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,
            AccessToken accessToken) {
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, sortOrder, offset, limit, accessToken);
            return collectionQueryCache.get(key);
        }
        return null;
    }

    @Override
    public void putCollectionQuery(String collectionNameOrQuery, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,
            AccessToken accessToken, CollectionQueryEntry queryEntry) {
        if (collectionQueryCache.size() > getCacheMaxSize()) {
            logger.warn("Collection query cache exceeds allowed cache size: " + getCacheMaxSize() + " records");
            return;
        }
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, sortOrder, offset, limit, accessToken);
            collectionQueryCache.put(key, queryEntry);
        }
    }

    @Override
    public CollectionQueryEntry getCollectionCountQuery(String collectionNameOrQuery, List<? extends Filter> filterValues, AccessToken accessToken) {
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, null, null, null, accessToken);
            return collectionQueryCache.get(key);
        }
        return null;
    }

    @Override
    public void putCollectionCountQuery(String collectionNameOrQuery, List<? extends Filter> filterValues, AccessToken accessToken, CollectionQueryEntry queryEntry) {
        if (collectionQueryCache.size() > getCacheMaxSize()) {
            logger.warn("Collection query cache exceeds allowed cache size: " + getCacheMaxSize() + " records");
            return;
        }
        if (collectionNameOrQuery != null) {
            CollectionQueryKey key = new CollectionQueryKey(collectionNameOrQuery, filterValues, null, null, null, accessToken);
            collectionQueryCache.put(key, queryEntry);
        }
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
    public CollectionQueryEntry getCollectionQuery(String collectionQuery, int offset, int limit, AccessToken accessToken) {
        return getCollectionQuery(collectionQuery, null, null, offset, limit, accessToken);
    }

    @Override
    public void putCollectionQuery(String collectionQuery, int offset, int limit, AccessToken accessToken, CollectionQueryEntry queryEntry) {
        putCollectionQuery(collectionQuery, null, null, offset, limit, accessToken, queryEntry);
    }
}
