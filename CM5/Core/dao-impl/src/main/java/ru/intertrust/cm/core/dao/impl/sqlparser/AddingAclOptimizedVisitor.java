package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.WithItem;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.impl.DomainObjectQueryHelper;

public class AddingAclOptimizedVisitor extends AddAclVisitor {

    private static final Map<String, QueryCacheData> queryCacheData = new ConcurrentHashMap<>();
    private static final Map<String, WithItem> aclWithItemsCache = new ConcurrentHashMap<>();

    private final Set<String> withItemsAdded = new HashSet<>();
    private QueryCacheData queryCacheDataLocalCache;

    public AddingAclOptimizedVisitor(ConfigurationExplorer configurationExplorer, UserGroupGlobalCache userGroupCache, CurrentUserAccessor currentUserAccessor, DomainObjectQueryHelper domainObjectQueryHelper) {
        super(configurationExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);
    }

    public static void clearCache() {
        queryCacheData.clear();
        aclWithItemsCache.clear();
        AddAclVisitor.aclWithItemStamps.set(null);
    }

    @Override
    protected void addAclWithItem() {
        final QueryCacheData queryCacheData = queryCacheDataLocalCache;
        final String withTableName = queryCacheData.withTableName;
        final WithItem withItem = aclWithItemLocalCache;

        if (withItem != null && !withItemsAdded.contains(withTableName)) {
            addWithItem(withItem);
            withItemsAdded.add(withTableName);
        }
    }

    @Override
    protected void readCacheToLocal(String tableName) {
        // Тут копия, судя по всему, не нужна
        queryCacheDataLocalCache = AddingAclOptimizedVisitor.queryCacheData.get(tableName);
        if (queryCacheDataLocalCache != null) {
            aclWithItemLocalCache = copyWithItem(aclWithItemsCache.get(queryCacheDataLocalCache.withTableName));
        }

        stampWithItemLocalCache = AddAclVisitor.aclWithItemStamps.get();
        if (stampWithItemLocalCache != null) {
            // DUMMY нельзя копировать, он должен всегда оставаться тем же объектом, но и менять его нельзя
            if (stampWithItemLocalCache != DUMMY) {
                stampWithItemLocalCache = copyWithItem(stampWithItemLocalCache);
            }
        }
    }

    @Override
    protected void cacheAclSelect(String tableName, Select aclSelect) {

        aclWithItemLocalCache = domainObjectQueryHelper.getAclWithItem(aclSelect);
        final String nameOfWith = getNameOfWith(aclWithItemLocalCache);
        aclWithItemsCache.putIfAbsent(nameOfWith, copyWithItem(aclWithItemLocalCache));

        queryCacheDataLocalCache = new QueryCacheData(aclSelect.getSelectBody(), nameOfWith);
        queryCacheData.putIfAbsent(tableName, queryCacheDataLocalCache);

        addStampCache(aclSelect);
    }

    @Override
    protected SelectBody getSelectBodyFromCache() {
        return queryCacheDataLocalCache.selectBody;
    }

    @Override
    protected boolean isCacheFilled() {
        return queryCacheDataLocalCache != null && stampWithItemLocalCache != null && aclWithItemLocalCache != null;
    }

    private String getNameOfWith(WithItem item) {
        return item.getName();
    }

    private static class QueryCacheData {
        private final SelectBody selectBody;
        private final String withTableName;

        private QueryCacheData(SelectBody selectBody, String withTableName) {
            this.selectBody = selectBody;
            this.withTableName = withTableName;
        }

        public QueryCacheData(QueryCacheData other) {
            this.selectBody = other.selectBody;
            this.withTableName = other.withTableName;
        }
    }


}
