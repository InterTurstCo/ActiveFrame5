package ru.intertrust.cm.core.dao.impl;

import net.sf.jsqlparser.statement.select.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.FilterForCache;
import ru.intertrust.cm.core.business.api.QueryModifierPrompt;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.Subject;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.impl.parameters.ParametersConverter;
import ru.intertrust.cm.core.dao.impl.sqlparser.CollectDOTypesVisitor;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryParser;
import ru.intertrust.cm.core.dao.impl.utils.CollectionRowMapper;

import java.util.*;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.REFERENCE_TYPE_POSTFIX;
import static ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier.wrapAndLowerCaseNames;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.setParameter;

/**
 * @author vmatsukevich Date: 7/1/13 Time: 6:58 PM
 */
public class CollectionsDaoImpl implements CollectionsDao {
    private static final Logger logger = LoggerFactory.getLogger(CollectionsDaoImpl.class);

    public static final String END_PARAM_SIGN = "_END_PARAM_'";
    public static final String START_PARAM_SIGN = "'_START_PARAM";
    public static final String PARAM_NAME_PREFIX = "_PARAM_NAME_";
    public static final String CURRENT_PERSON_PARAM = "CURRENT_PERSON";

    public static final String JDBC_PARAM_PREFIX = "PARAM";

    private static final String PARAM_NAME_PREFIX_SPRING = ":";

    public static final String IDS_EXCLUDED_FILTER_PREFIX = "idsExcluded";
    public static final String IDS_INCLUDED_FILTER_PREFIX = "idsIncluded";

    private static final AccessToken QUERY_ACCESS_TOKEN = new AccessToken() {
        @Override
        public Subject getSubject() {
            return null;
        }

        @Override
        public boolean isDeferred() {
            return false;
        }

        @Override
        public AccessLimitationType getAccessLimitationType() {
            return null;
        }
    };

    @Autowired
    private CollectionQueryCache collectionQueryCache;

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    private GlobalCacheClient globalCacheClient;

    @Autowired
    private GlobalCacheManager globalCacheManager;

    @Autowired
    private CollectionsCacheServiceImpl collectionsCacheService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private UserGroupGlobalCache userGroupCache;

    @Autowired
    private ServerComponentService serverComponentService;

    @Autowired
    protected DomainObjectQueryHelper domainObjectQueryHelper;

    public CurrentUserAccessor getCurrentUserAccessor() {
        return currentUserAccessor;
    }

    public UserGroupGlobalCache getUserGroupCache() {
        return userGroupCache;
    }

    public void setJdbcTemplate(NamedParameterJdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setCurrentUserAccessor(CurrentUserAccessor currentUserAccessor) {
        this.currentUserAccessor = currentUserAccessor;
    }

    public void setDomainObjectTypeIdCache(DomainObjectTypeIdCache domainObjectTypeIdCache) {
        this.domainObjectTypeIdCache = domainObjectTypeIdCache;
    }

    public void setCollectionsCacheService(CollectionsCacheServiceImpl collectionsCacheService) {
        this.collectionsCacheService = collectionsCacheService;
    }

    public void setCollectionQueryCache(CollectionQueryCache collectionQueryCache) {
        this.collectionQueryCache = collectionQueryCache;
    }

    public void setDomainObjectQueryHelper(DomainObjectQueryHelper domainObjectQueryHelper) {
        this.domainObjectQueryHelper = domainObjectQueryHelper;
    }

    /**
     * Устанавливает {@link #configurationExplorer}
     * @param configurationExplorer
     *            {@link #configurationExplorer}
     */
    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    public void setGlobalCacheClient(GlobalCacheClient globalCacheClient) {
        this.globalCacheClient = globalCacheClient;
    }

    /*
     * {@see
     * ru.intertrust.cm.core.dao.api.DomainObjectDao#findCollection(ru.intertrust
     * .cm.core.config.model. CollectionNestedConfig, java.util.List,
     * ru.intertrust.cm.core.business.api.dto.SortOrder, int, int)}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName,
            List<? extends Filter> filterValues,
            SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        long start = System.nanoTime();

        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);

        if (collectionConfig.getTransactionCache() == CollectionConfig.TransactionCacheType.enabled) {
            IdentifiableObjectCollection fromCache = collectionsCacheService.getCollectionFromCache(collectionName, filterValues, sortOrder, offset, limit);
            if (fromCache != null) {
                return fromCache;
            }
        }

        if (collectionConfig.getGenerator() != null) { // generated collections
                                                       // aren't supported by
                                                       // global cache yet
            String collectionGeneratorComponent = collectionConfig.getGenerator().getClassName();
            return getCollectionFromGenerator(collectionGeneratorComponent, filterValues, sortOrder, offset, limit);
        }

        List<Filter> processedFilterValues = processIdsFilters(filterValues);
        checkFilterValues(processedFilterValues);

        final IdentifiableObjectCollection fromGlobalCache = globalCacheClient.getCollection(collectionName, processedFilterValues, sortOrder, offset, limit,
                accessToken);
        if (fromGlobalCache != null) {
            return validateCache(collectionName, processedFilterValues, sortOrder, offset, limit, accessToken, start, fromGlobalCache);
        }

        final Pair<IdentifiableObjectCollection, Long> dbResultAndStart = findCollectionInDB(start, collectionName, processedFilterValues, sortOrder, offset, limit,
                accessToken);
        final IdentifiableObjectCollection collection = dbResultAndStart.getFirst();

        if (collectionConfig.getTransactionCache() == CollectionConfig.TransactionCacheType.enabled) {
            collectionsCacheService.putCollectionToCache(collection, collectionName, processedFilterValues, sortOrder, offset, limit);
        }
        globalCacheClient.notifyCollectionRead(collectionName, processedFilterValues, sortOrder, offset, limit, collection, dbResultAndStart.getSecond(), accessToken);
        return collection;
    }

    private Pair<IdentifiableObjectCollection, Long> findCollectionInDB(long preparationStartTime, String collectionName,
            List<? extends Filter> filterValues,
            SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        String collectionQuery;
        Map<String, FieldConfig> columnToConfigMapForSelectItems;

        Map<String, Object> parameters = new HashMap<>();
        ParametersConverter converter = new ParametersConverter();
        Pair<Map<String, Object>, QueryModifierPrompt> paramsWithPrompt = converter.convertReferenceValuesInFilters(filterValues);
        parameters.putAll(paramsWithPrompt.getFirst());

        CollectionQueryEntry cachedQueryEntry = collectionQueryCache.getCollectionQuery(collectionName, filtersForCache(filterValues),
                paramsWithPrompt.getSecond(), sortOrder, offset,
                limit, accessToken);

        if (cachedQueryEntry != null) {
            collectionQuery = cachedQueryEntry.getQuery();
            columnToConfigMapForSelectItems = cachedQueryEntry.getColumnToConfigMap();

        } else {
            collectionQuery =
                    getFindCollectionQuery(collectionConfig, filterValues, sortOrder, offset, limit, accessToken);

            SqlQueryParser sqlParser = new SqlQueryParser(collectionQuery);
            Select select = sqlParser.getSelectStatement();

            SqlQueryModifier sqlQueryModifier = createSqlQueryModifier();

            Map<String, FieldConfig> columnToConfigMap =
                    sqlQueryModifier.buildColumnToConfigMapForParameters(select);
            columnToConfigMapForSelectItems = createSqlQueryModifier().buildColumnToConfigMapForSelectItems(select);

            columnToConfigMap.putAll(columnToConfigMapForSelectItems);

            collectionQuery = sqlQueryModifier.modifyQueryWithReferenceFilterValues(select, paramsWithPrompt.getSecond());

            collectionQuery = adjustParameterNamesAfterPreProcessing(collectionQuery);
            collectionQuery = wrapAndLowerCaseNames(new SqlQueryParser(collectionQuery).getSelectStatement());
            CollectionQueryEntry collectionQueryEntry = new CollectionQueryEntry(collectionQuery, columnToConfigMapForSelectItems);
            collectionQueryCache.putCollectionQuery(collectionName, filtersForCache(filterValues), paramsWithPrompt.getSecond(), sortOrder, offset, limit,
                    accessToken, collectionQueryEntry);
        }

        fillFilterParameters(filterValues, parameters);

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }

        addCurrentPersonParameter(collectionQuery, parameters);

        long preparationTime = System.nanoTime() - preparationStartTime;
        SqlLogger.SQL_PREPARATION_TIME_CACHE.set(preparationTime);

        long retrieveTime = System.currentTimeMillis();

        return new Pair<>(jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(collectionName, columnToConfigMapForSelectItems, collectionConfig.getIdField(),
                        configurationExplorer, domainObjectTypeIdCache)), retrieveTime);
    }

    private Set<FilterForCache> filtersForCache(List<? extends Filter> filterValues) {
        HashSet<FilterForCache> filtersForCache = new HashSet<>();
        for (Filter f : filterValues) {
            filtersForCache.add(new FilterForCache(f));
        }
        return filtersForCache;
    }

    private Pair<Integer, Long> findCollectionCountInDB(long preparationStartTime, CollectionConfig collectionConfig, String collectionName,
            List<? extends Filter> filterValues, AccessToken accessToken) {

        Map<String, Object> parameters = new HashMap<>();
        ParametersConverter converter = new ParametersConverter();
        Pair<Map<String, Object>, QueryModifierPrompt> paramsWithPrompt = converter.convertReferenceValuesInFilters(filterValues);
        parameters.putAll(paramsWithPrompt.getFirst());

        String collectionQuery;
        CollectionQueryEntry cachedQueryEntry = collectionQueryCache.getCollectionCountQuery(collectionName, filtersForCache(filterValues),
                paramsWithPrompt.getSecond(), accessToken);

        if (cachedQueryEntry != null) {
            collectionQuery = cachedQueryEntry.getQuery();
        } else {

            collectionQuery = getFindCollectionCountQuery(collectionConfig, filterValues, accessToken);

            SqlQueryParser sqlParser = new SqlQueryParser(collectionQuery);
            Select select = sqlParser.getSelectStatement();
            SqlQueryModifier sqlQueryModifier = createSqlQueryModifier();
            collectionQuery = sqlQueryModifier.modifyQueryWithReferenceFilterValues(select, paramsWithPrompt.getSecond());

            collectionQuery = adjustParameterNamesAfterPreProcessing(collectionQuery);
            collectionQuery = wrapAndLowerCaseNames(new SqlQueryParser(collectionQuery).getSelectStatement());
            CollectionQueryEntry collectionQueryEntry = new CollectionQueryEntry(collectionQuery, null);
            collectionQueryCache.putCollectionCountQuery(collectionName, filtersForCache(filterValues), paramsWithPrompt.getSecond(), accessToken,
                    collectionQueryEntry);
        }

        fillFilterParameters(filterValues, parameters);

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }
        long preparationTime = System.nanoTime() - preparationStartTime;
        SqlLogger.SQL_PREPARATION_TIME_CACHE.set(preparationTime);

        long retrieveTime = System.currentTimeMillis();
        return new Pair<>(jdbcTemplate.queryForObject(collectionQuery, parameters, Integer.class), retrieveTime);
    }

    private IdentifiableObjectCollection validateCache(String collectionName, List<? extends Filter> filterValues, SortOrder sortOrder, int offset, int limit,
            AccessToken accessToken, long start, IdentifiableObjectCollection fromGlobalCache) {
        if (globalCacheManager.isDebugEnabled()) {
            IdentifiableObjectCollection fromDb = findCollectionInDB(start, collectionName, filterValues, sortOrder, offset, limit, accessToken).getFirst();
            if (!fromGlobalCache.equals(fromDb)) {
                logger.error("CACHE ERROR! Named collection: " + collectionName);
            }
        }
        return fromGlobalCache;
    }

    private Integer validateCountCache(CollectionConfig config, String collectionName, List<? extends Filter> filterValues, AccessToken accessToken,
            long start, Integer fromGlobalCache) {
        if (globalCacheManager.isDebugEnabled()) {
            Integer fromDb = findCollectionCountInDB(start, config, collectionName, filterValues, accessToken).getFirst();
            if (!fromGlobalCache.equals(fromDb)) {
                logger.error("CACHE ERROR! Named collection: " + collectionName);
            }
        }
        return fromGlobalCache;
    }

    /**
     * Возвращает доменные объекты, воздействующие на коллекцию
     * @param collectionName
     *            название коллекции
     * @param filterValues
     *            значения фильтров
     * @return пару, первое значения которой - имена фильтров, второе - типы
     *         объектов
     */
    @Override
    public Pair<Set<String>, Set<String>> getDOTypes(String collectionName, List<? extends Filter> filterValues) {
        final Set<String> filterNames = ModelUtil.getFilterNames(filterValues);
        Set<String> result = collectionQueryCache.getCollectionDomainObjectTypes(collectionName, filterNames);
        if (result != null) {
            return new Pair<>(filterNames, result);
        }
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);
        String preparedQuery = getFindCollectionQuery(collectionConfig, filterValues, null, 0, 0, QUERY_ACCESS_TOKEN);
        result = new CollectDOTypesVisitor(configurationExplorer).getDOTypes(preparedQuery);
        collectionQueryCache.putCollectionDomainObjectTypes(collectionName, filterNames, result);
        return new Pair<>(filterNames, result);
    }

    @Override
    public Set<String> getQueryDOTypes(String collectionQuery) {
        Set<String> result = collectionQueryCache.getCollectionDomainObjectTypes(collectionQuery);
        if (result != null) {
            return result;
        }
        CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);
        String preparedQuery = adjustParameterNamesBeforePreProcessing(collectionQuery, PARAM_NAME_PREFIX);
        preparedQuery = collectionQueryInitializer.initializeQuery(preparedQuery, 0, 0, QUERY_ACCESS_TOKEN);
        result = new CollectDOTypesVisitor(configurationExplorer).getDOTypes(preparedQuery);
        collectionQueryCache.putCollectionDomainObjectTypes(collectionQuery, result);
        return result;
    }

    private List<Filter> processIdsFilters(List<? extends Filter> filterValues) {
        if (filterValues == null) {
            return null;
        }
        List<Filter> processedFilters = new ArrayList<>();

        for (Filter filter : filterValues) {
            if (filter instanceof IdsIncludedFilter || filter instanceof IdsExcludedFilter) {
                continue;
            }
            processedFilters.add(filter);
        }

        List<IdsIncludedFilter> idsIncludedFilters = new ArrayList<>();
        List<IdsExcludedFilter> idsExcludedFilters = new ArrayList<>();

        for (Filter filter : filterValues) {
            if (filter instanceof IdsIncludedFilter) {
                idsIncludedFilters.add((IdsIncludedFilter) filter);
            }
            if (filter instanceof IdsExcludedFilter) {
                idsExcludedFilters.add((IdsExcludedFilter) filter);
            }

        }

        int index = 0;
        for (IdsIncludedFilter idsIncludedFilter : idsIncludedFilters) {
            Filter clonedFilter = new Filter();
            clonedFilter.setFilter(IDS_INCLUDED_FILTER_PREFIX + index);
            clonedFilter.addCriterion(0, mergeCriterions(idsIncludedFilter));
            processedFilters.add(clonedFilter);
            index++;
        }

        index = 0;
        for (IdsExcludedFilter idsExcludedFilter : idsExcludedFilters) {
            if (idsExcludedFilter.getParameterMap().isEmpty()) {
                continue;
            }
            Filter clonedFilter = new Filter();
            clonedFilter.setFilter(IDS_EXCLUDED_FILTER_PREFIX + index);
            clonedFilter.addCriterion(0, mergeCriterions(idsExcludedFilter));
            processedFilters.add(clonedFilter);
            index++;
        }
        return processedFilters;
    }

    private ListValue mergeCriterions(IdBasedFilter filter) {
        ArrayList<ReferenceValue> values = new ArrayList<ReferenceValue>();
        for (int criterion : filter.getCriterionKeys()) {
            values.add(filter.getCriterion(criterion));
        }
        return ListValue.createListValue(values);
    }

    private IdentifiableObjectCollection getCollectionFromGenerator(String collectionGeneratorComponent, List<? extends Filter> filterValues,
            SortOrder sortOrder, int offset, int limit) {
        CollectionDataGenerator collectionDataGenerator = (CollectionDataGenerator) serverComponentService.getServerComponent(collectionGeneratorComponent);
        return collectionDataGenerator.findCollection(filterValues, sortOrder, offset, limit);
    }

    /**
     * При запросе коллекции с набором фильтров, если в этом наборе дважды
     * встречается фильтр с одним и тем же именем, следует выбрасывать
     * исключение IllegalArgumentException.
     * @param filterValues
     *            набор фильтров
     */
    private void checkFilterValues(List<? extends Filter> filterValues) {
        if (filterValues != null && !filterValues.isEmpty()) {
            Set<String> names = new HashSet<>((int) (filterValues.size() / 0.75f) + 1);
            for (Filter filterValue : filterValues) {
                String filterName = filterValue.getFilter();
                if (names.contains(filterName)) {
                    throw new IllegalArgumentException("Filter values have duplicate filter name:" + filterName);
                }
                names.add(filterName);
            }
        }
    }

    private void addCurrentPersonParameter(String collectionQuery, Map<String, Object> parameters) {
        if (collectionQuery.indexOf(CURRENT_PERSON_PARAM) > 0) {
            Id personId = getCurrentUserId();
            parameters.put(CURRENT_PERSON_PARAM, ((RdbmsId) personId).getId());
        }
    }

    private Id getCurrentUserId() {
        return currentUserAccessor.getCurrentUserId();
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, int offset, int limit,
            AccessToken accessToken) {

        long start = System.nanoTime();
        final IdentifiableObjectCollection fromGlobalCache = globalCacheClient.getCollection(query, null, offset, limit, accessToken);
        if (fromGlobalCache != null) {
            return validateCache(query, offset, limit, accessToken, start, fromGlobalCache);
        }

        final Pair<IdentifiableObjectCollection, Long> result = findCollectionByQueryInDB(start, query, offset, limit, accessToken);
        final IdentifiableObjectCollection collection = result.getFirst();
        globalCacheClient.notifyCollectionRead(query, null, offset, limit, collection, result.getSecond(), accessToken);
        return collection;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, List<? extends Value> params,
            int offset, int limit, AccessToken accessToken) {
        long start = System.nanoTime();
        final IdentifiableObjectCollection fromGlobalCache = globalCacheClient.getCollection(query, params, offset, limit, accessToken);
        if (fromGlobalCache != null) {
            return validateCache(query, params, offset, limit, accessToken, start, fromGlobalCache);
        }

        final Pair<IdentifiableObjectCollection, Long> result = findCollectionByQueryInDB(start, query, params, offset, limit, accessToken);
        final IdentifiableObjectCollection collection = result.getFirst();
        globalCacheClient.notifyCollectionRead(query, params, offset, limit, collection, result.getSecond(), accessToken);
        return collection;
    }

    public Pair<IdentifiableObjectCollection, Long> findCollectionByQueryInDB(long preparationStartTime, String query, int offset, int limit,
            AccessToken accessToken) {

        Map<String, Object> parameters = new HashMap<>();

        String collectionQuery;
        Map<String, FieldConfig> columnToConfigMapForSelectItems;
        CollectionQueryEntry cachedQueryEntry = collectionQueryCache.getCollectionQuery(query, offset, limit, null, accessToken);

        if (cachedQueryEntry != null) {
            collectionQuery = cachedQueryEntry.getQuery();
            columnToConfigMapForSelectItems = cachedQueryEntry.getColumnToConfigMap();
        } else {

            CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);
            collectionQuery = collectionQueryInitializer.initializeQuery(query, offset, limit, accessToken);

            SqlQueryParser sqlParser = new SqlQueryParser(collectionQuery);
            Select select = sqlParser.getSelectStatement();

            columnToConfigMapForSelectItems =
                    createSqlQueryModifier().buildColumnToConfigMapForSelectItems(select);

            collectionQuery = wrapAndLowerCaseNames(select);
            CollectionQueryEntry collectionQueryEntry = new CollectionQueryEntry(collectionQuery, columnToConfigMapForSelectItems);
            collectionQueryCache.putCollectionQuery(query, offset, limit, null, accessToken, collectionQueryEntry);

        }

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }
        long preparationTime = System.nanoTime() - preparationStartTime;
        SqlLogger.SQL_PREPARATION_TIME_CACHE.set(preparationTime);

        long retrieveTime = System.currentTimeMillis();

        return new Pair<>(jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(columnToConfigMapForSelectItems, configurationExplorer, domainObjectTypeIdCache)), retrieveTime);
    }

    @SuppressWarnings("unchecked")
    private Pair<IdentifiableObjectCollection, Long> findCollectionByQueryInDB(long preparationStartTime, String query, List<? extends Value> params,
            int offset, int limit, AccessToken accessToken) {
        Map<String, Object> parameters = new HashMap<>();
        ParametersConverter converter = new ParametersConverter();
        Pair<Map<String, Object>, QueryModifierPrompt> paramsWithPrompt = converter.convertReferenceValues((List<Value<?>>) params);
        parameters.putAll(paramsWithPrompt.getFirst());

        String collectionQuery;
        Map<String, FieldConfig> columnToConfigMapForSelectItems;
        CollectionQueryEntry cachedQueryEntry = collectionQueryCache.getCollectionQuery(query, offset, limit, null, accessToken);

        if (cachedQueryEntry != null) {
            collectionQuery = cachedQueryEntry.getQuery();
            columnToConfigMapForSelectItems = cachedQueryEntry.getColumnToConfigMap();

        } else {
            CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);

            collectionQuery = adjustParameterNamesBeforePreProcessing(query, CollectionsDaoImpl.PARAM_NAME_PREFIX);
            collectionQuery = collectionQueryInitializer.initializeQuery(collectionQuery, offset, limit, accessToken);

            SqlQueryParser sqlParser = new SqlQueryParser(collectionQuery);
            Select select = sqlParser.getSelectStatement();

            SqlQueryModifier sqlQueryModifier = createSqlQueryModifier();

            Map<String, FieldConfig> columnToConfigMap = sqlQueryModifier.buildColumnToConfigMapForParameters(select);
            columnToConfigMapForSelectItems = sqlQueryModifier.buildColumnToConfigMapForSelectItems(select);

            // нужно объединить конфигурации колонок из Select части запроса и и
            // для параметров для случая, когда в
            // параметре указывается алиас колонки из подзапроса
            columnToConfigMap.putAll(columnToConfigMapForSelectItems);

            collectionQuery = sqlQueryModifier.modifyQueryWithParameters(select, paramsWithPrompt.getSecond());
            collectionQuery = wrapAndLowerCaseNames(new SqlQueryParser(collectionQuery).getSelectStatement());
            collectionQuery = adjustParameterNamesAfterPreProcessing(collectionQuery);

            CollectionQueryEntry collectionQueryEntry = new CollectionQueryEntry(collectionQuery, columnToConfigMapForSelectItems);
            collectionQueryCache.putCollectionQuery(query, offset, limit, null, accessToken, collectionQueryEntry);
        }

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }
        fillParameterMap(params, parameters);

        long preparationTime = System.nanoTime() - preparationStartTime;
        SqlLogger.SQL_PREPARATION_TIME_CACHE.set(preparationTime);

        long retrieveTime = System.currentTimeMillis();
        return new Pair<>(jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(columnToConfigMapForSelectItems, configurationExplorer, domainObjectTypeIdCache)), retrieveTime);
    }

    private IdentifiableObjectCollection validateCache(String query, int offset, int limit,
            AccessToken accessToken, long start, IdentifiableObjectCollection fromGlobalCache) {
        if (globalCacheManager.isDebugEnabled()) {
            IdentifiableObjectCollection fromDb = findCollectionByQueryInDB(start, query, offset, limit, accessToken).getFirst();
            if (!fromGlobalCache.equals(fromDb)) {
                logger.error("CACHE ERROR! Query: " + query);
            }
        }
        return fromGlobalCache;
    }

    private IdentifiableObjectCollection validateCache(String query, List<? extends Value> params, int offset, int limit,
            AccessToken accessToken, long start, IdentifiableObjectCollection fromGlobalCache) {
        if (globalCacheManager.isDebugEnabled()) {
            IdentifiableObjectCollection fromDb = findCollectionByQueryInDB(start, query, params, offset, limit, accessToken).getFirst();
            if (!fromGlobalCache.equals(fromDb)) {
                logger.error("CACHE ERROR! Query: " + query);
            }
        }
        return fromGlobalCache;
    }

    private SqlQueryModifier createSqlQueryModifier() {
        return new SqlQueryModifier(configurationExplorer, userGroupCache, currentUserAccessor, domainObjectQueryHelper);
    }

    private void addParametersForReference(Map<String, Object> parameters, ReferenceValue referenceValue, String referenceParam) {
        String referenceTypeParam = referenceParam + DomainObjectDao.REFERENCE_TYPE_POSTFIX;

        long refId = ((RdbmsId) referenceValue.get()).getId();
        long refTypeId = ((RdbmsId) referenceValue.get()).getTypeId();

        parameters.put(referenceParam, refId);
        parameters.put(referenceTypeParam, refTypeId);
    }

    public static String adjustParameterNames(String subQuery, String parameterPrefix) {
        String newFilterCriteria = subQuery.replace("{", parameterPrefix);
        newFilterCriteria = newFilterCriteria.replace("}", "");

        return newFilterCriteria;
    }

    public static String adjustParameterNamesBeforePreProcessing(String subQuery, String parameterPrefix) {
        String newFilterCriteria = subQuery.replaceAll("[{]", START_PARAM_SIGN + parameterPrefix);
        newFilterCriteria = newFilterCriteria.replaceAll("[}]", END_PARAM_SIGN);
        return newFilterCriteria;
    }

    /*
     * {@see
     * ru.intertrust.cm.core.dao.api.DomainObjectDao#findCollectionCountByQuery
     * (ru.intertrust.cm.core.config.model. CollectionNestedConfig ,
     * java.util.List, ru.intertrust.cm.core.business.api.dto.SortOrder)}
     */
    @Override
    public int findCollectionCount(String collectionName,
            List<? extends Filter> filterValues, AccessToken accessToken) {

        long start = System.nanoTime();

        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);

        if (collectionConfig.getGenerator() != null) {
            String collectionGeneratorComponent = collectionConfig.getGenerator().getClassName();
            CollectionDataGenerator collectionDataGenerator = (CollectionDataGenerator) serverComponentService.getServerComponent(collectionGeneratorComponent);
            return collectionDataGenerator.findCollectionCount(filterValues);
        }

        List<Filter> processedFilterValues = processIdsFilters(filterValues);
        checkFilterValues(processedFilterValues);
        final int fromGlobalCache = globalCacheClient.getCollectionCount(collectionName, processedFilterValues, accessToken);
        if (fromGlobalCache != -1) {
            return validateCountCache(collectionConfig, collectionName, processedFilterValues, accessToken, start, fromGlobalCache);
        }

        final Pair<Integer, Long> dbResultAndStart = findCollectionCountInDB(start, collectionConfig, collectionName, processedFilterValues, accessToken);
        final Integer count = dbResultAndStart.getFirst();

        globalCacheClient.notifyCollectionCountRead(collectionName, processedFilterValues, count, dbResultAndStart.getSecond(), accessToken);
        return count;
    }

    protected CollectionQueryInitializer createCollectionQueryInitializer(ConfigurationExplorer configurationExplorer) {
        return new CollectionQueryInitializerImpl(configurationExplorer, userGroupCache,
                currentUserAccessor, domainObjectQueryHelper);
    }

    /**
     * Возвращает запрос, который используется в методе поиска коллекции
     * доменных объектов
     */
    protected String getFindCollectionQuery(CollectionConfig collectionConfig,
            List<? extends Filter> filterValues, SortOrder sortOrder,
            int offset, int limit, AccessToken accessToken) {
        CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);

        return collectionQueryInitializer.initializeQuery(collectionConfig, filterValues,
                sortOrder, offset, limit, accessToken);
    }

    /**
     * Возвращает запрос, который используется в методе поиска количества
     * объектов в коллекции
     */
    protected String getFindCollectionCountQuery(CollectionConfig collectionConfig,
            List<? extends Filter> filterValues, AccessToken accessToken) {
        CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);

        return collectionQueryInitializer.initializeCountQuery(collectionConfig, filterValues, accessToken);
    }

    private void fillParameterMap(List<? extends Value> params, Map<String, Object> parameterMap) {
        int index = 0;
        for (Value value : params) {
            // ссылочные параметры не добавляются в список параметров, т.к. их
            // значения подставляются прямо в запрос
            if (!(value instanceof ReferenceValue)) {
                // в исполняемом SQL запросе названия параметров совпадает с их
                // номерами в начальном SQL запросе.
                String parameterName = index + "";
                setParameter(parameterName, value, parameterMap, true);
            }
            index++;
        }
    }

    private void fillAclParameters(AccessToken accessToken, Map<String, Object> parameters) {
        parameters.put("user_id", ((UserSubject) accessToken.getSubject()).getUserId());
    }

    /**
     * Модифицирует имена параметров в названия совместимые с {@see
     * NamedParameterJdbcTemplate}. Заменяет префикс "_PARAM_NAME_" на ":"
     * @param collectionQuery
     *            SQL запрос
     */
    private String adjustParameterNamesForSpring(String collectionQuery) {
        collectionQuery = collectionQuery.replaceAll(PARAM_NAME_PREFIX, PARAM_NAME_PREFIX_SPRING);
        return collectionQuery;
    }

    private String adjustParameterNamesAfterPreProcessing(String collectionQuery) {
        collectionQuery = collectionQuery.replaceAll(START_PARAM_SIGN, "");
        collectionQuery = collectionQuery.replaceAll(END_PARAM_SIGN, "");

        collectionQuery = adjustParameterNamesForSpring(collectionQuery);
        return collectionQuery;
    }

    /**
     * Заполняет параметры. Имена параметров в формате имя_фильтра + ключ
     * парметра, указанный в конфигурации.
     */
    private void fillFilterParameters(List<? extends Filter> filterValues, Map<String, Object> parameters) {
        if (filterValues != null) {
            for (Filter filter : filterValues) {
                for (Integer key : filter.getCriterionKeys()) {
                    String parameterName = filter.getFilter() + key;

                    Object criterion = getFilterCriterion(filter, key);

                    if (criterion == null) {
                        parameters.put(parameterName, null);
                    }
                    // //ссылочные параметры
                    // if(!(filter instanceof IdsIncludedFilter) && !(filter
                    // instanceof IdsExcludedFilter) && criterion instanceof
                    // ReferenceValue){
                    // continue;
                    // }
                    if (criterion instanceof Value) {
                        setParameter(parameterName, (Value<?>) criterion, parameters, true);
                    } else if (criterion instanceof List) {
                        List<Value> valuesList = (List) criterion;
                        if (doesNotContainReferenceValues(valuesList)) {
                            List<Object> parameterValues = getParameterValues(valuesList);
                            parameters.put(parameterName, parameterValues);
                        }
                    }

                    if (filter instanceof IdsIncludedFilter || filter instanceof IdsExcludedFilter) {
                        parameterName = filter.getFilter() + key + REFERENCE_TYPE_POSTFIX;
                        ReferenceValue referenceValue = (ReferenceValue) criterion;
                        parameters.put(parameterName, ((RdbmsId) referenceValue.get()).getTypeId());
                    }
                }
            }
        }
    }

    private boolean doesNotContainReferenceValues(List<?> values) {
        for (Object o : values) {
            if (o instanceof ReferenceValue) {
                return false;
            }
        }
        return true;
    }

    private List<Object> getParameterValues(List<Value> valuesList) {
        List<Object> parameterValues = new ArrayList<Object>();

        for (Value value : valuesList) {
            Object parameterValue = getParameterValue(value);
            parameterValues.add(parameterValue);

        }
        return parameterValues;
    }

    private Object getFilterCriterion(Filter filter, Integer key) {
        Object criterion = filter.getCriterion(key);
        if (criterion == null) {
            criterion = filter.getMultiCriterion(key);
        }
        return criterion;
    }

    private Object getParameterValue(Value value) {
        Object parameterValue;
        if (value instanceof ReferenceValue) {
            parameterValue = ((RdbmsId) value.get()).getId();
        } else {
            parameterValue = value.get();
        }
        return parameterValue;
    }
}
