package ru.intertrust.cm.core.dao.impl;

import net.sf.jsqlparser.statement.select.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.access.UserGroupGlobalCache;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.*;
import ru.intertrust.cm.core.dao.api.component.CollectionDataGenerator;
import ru.intertrust.cm.core.dao.impl.sqlparser.ReferenceFilterUtility;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier;
import ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryParser;
import ru.intertrust.cm.core.dao.impl.utils.CollectionRowMapper;

import java.util.*;

import static ru.intertrust.cm.core.dao.api.DomainObjectDao.REFERENCE_TYPE_POSTFIX;
import static ru.intertrust.cm.core.dao.impl.sqlparser.SqlQueryModifier.wrapAndLowerCaseNames;
import static ru.intertrust.cm.core.dao.impl.utils.DaoUtils.setParameter;

/**
 * @author vmatsukevich
 *         Date: 7/1/13
 *         Time: 6:58 PM
 */
public class CollectionsDaoImpl implements CollectionsDao {

    public static final String END_PARAM_SIGN = "_END_PARAM_'";
    public static final String START_PARAM_SIGN = "'_START_PARAM";
    public static final String PARAM_NAME_PREFIX = "_PARAM_NAME_";
    public static final String CURRENT_PERSON_PARAM = "CURRENT_PERSON";

    public static final String JDBC_PARAM_PREFIX = "PARAM";
    
    private static final String PARAM_NAME_PREFIX_SPRING = ":";

    public static final String IDS_EXCLUDED_FILTER_PREFIX = "idsExcluded";
    public static final String IDS_INCLUDED_FILTER_PREFIX = "idsIncluded";

    @Autowired
    private CollectionQueryCache collectionQueryCache;

    @Autowired
    @Qualifier("switchableNamedParameterJdbcTemplate")
    private NamedParameterJdbcOperations jdbcTemplate;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private CollectionsCacheServiceImpl collectionsCacheService;

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private UserGroupGlobalCache userGroupCache;

    @Autowired
    private ServerComponentService serverComponentService;

    
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

    /**
     * Устанавливает {@link #configurationExplorer}
     * @param configurationExplorer {@link #configurationExplorer}
     */
    public void setConfigurationExplorer(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /*
     * {@see ru.intertrust.cm.core.dao.api.DomainObjectDao#findCollection(ru.intertrust.cm.core.config.model.
     * CollectionNestedConfig, java.util.List, ru.intertrust.cm.core.business.api.dto.SortOrder, int, int)}
     */
    @Override
    public IdentifiableObjectCollection findCollection(String collectionName,
            List<? extends Filter> filterValues,
            SortOrder sortOrder, int offset, int limit, AccessToken accessToken) {
        long start = System.nanoTime();

        filterValues = processIdsFilters(filterValues);
        checkFilterValues(filterValues);
        
        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);

        if (collectionConfig.getTransactionCache() == CollectionConfig.TransactionCacheType.enabled) {
            IdentifiableObjectCollection fromCache = collectionsCacheService.getCollectionFromCache(collectionName, filterValues, sortOrder, offset, limit);
            if (fromCache != null) {
                return fromCache;
            }
        }

        if (collectionConfig.getGenerator() != null) {
            String collectionGeneratorComponent = collectionConfig.getGenerator().getClassName();
            return getCollectionFromGenerator(collectionGeneratorComponent, filterValues, sortOrder, offset, limit);
        }
        String collectionQuery;
        Map<String, FieldConfig> columnToConfigMapForSelectItems;
        CollectionQueryEntry cachedQueryEntry = collectionQueryCache.getCollectionQuery(collectionName, filterValues, sortOrder, offset, limit, accessToken);

        Map<String, Object> parameters = new HashMap<>();

        if (cachedQueryEntry != null) {
            collectionQuery = cachedQueryEntry.getQuery();
            columnToConfigMapForSelectItems = cachedQueryEntry.getColumnToConfigMap();

            addReferenceFilterParameters(filterValues, parameters);

        } else {
            collectionQuery =
                    getFindCollectionQuery(collectionConfig, filterValues, sortOrder, offset, limit, accessToken);

            SqlQueryParser sqlParser = new SqlQueryParser(collectionQuery);
            Select select = sqlParser.getSelectStatement();

            Map<String, FieldConfig> columnToConfigMap =
                    createSqlQueryModifier().buildColumnToConfigMapForParameters(select);
            columnToConfigMapForSelectItems = createSqlQueryModifier().buildColumnToConfigMapForSelectItems(select);

            columnToConfigMap.putAll(columnToConfigMapForSelectItems);

            SqlQueryModifier sqlQueryModifier = createSqlQueryModifier();
            collectionQuery = sqlQueryModifier.modifyQueryWithReferenceFilterValues(select, filterValues, columnToConfigMap, parameters);

            collectionQuery = adjustParameterNamesForSpring(collectionQuery);
            collectionQuery = wrapAndLowerCaseNames(new SqlQueryParser(collectionQuery).getSelectStatement());
            CollectionQueryEntry collectionQueryEntry = new CollectionQueryEntry(collectionQuery, columnToConfigMapForSelectItems);
            collectionQueryCache.putCollectionQuery(collectionName, filterValues, sortOrder, offset, limit, accessToken, collectionQueryEntry);
        }

        fillFilterParameters(filterValues, parameters, false);

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }

        addCurrentPersonParameter(collectionQuery, parameters);

        long preparationTime = System.nanoTime() - start;
        SqlLogger.SQL_PREPARATION_TIME_CACHE.set(preparationTime);

        IdentifiableObjectCollection collection = jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(collectionName, columnToConfigMapForSelectItems, collectionConfig.getIdField(),
                        configurationExplorer, domainObjectTypeIdCache));

        if (collectionConfig.getTransactionCache() == CollectionConfig.TransactionCacheType.enabled) {
            collectionsCacheService.putCollectionToCache(collection, collectionName, filterValues, sortOrder, offset, limit);
        }

        return collection;
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

        List<IdsIncludedFilter> idsIncludedFilters = new ArrayList<>(1);
        List<IdsExcludedFilter> idsExcludedFilters = new ArrayList<>(1);

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
            IdsIncludedFilter clonedFilter = new IdsIncludedFilter(idsIncludedFilter);
            clonedFilter.setFilter(IDS_INCLUDED_FILTER_PREFIX + index);
            processedFilters.add(clonedFilter);
            index++;
        }
        
        index = 0;
        for (IdsExcludedFilter idsExcludedFilter : idsExcludedFilters) {
            IdsExcludedFilter clonedFilter = new IdsExcludedFilter(idsExcludedFilter);
            clonedFilter.setFilter(IDS_EXCLUDED_FILTER_PREFIX + index);
            processedFilters.add(clonedFilter);
            index++;
        }
        return processedFilters;
    }

    private void addReferenceFilterParameters(List<? extends Filter> filterValues, Map<String, Object> parameters) {
        if (filterValues != null) {
            for (Filter filterValue : filterValues) {
                for (Integer criterionKey : filterValue.getCriterionKeys()) {
                    Value criterionValue = filterValue.getCriterion(criterionKey);
                    if (criterionValue instanceof ReferenceValue) {
                        String referenceParam = filterValue.getFilter() + "_" + criterionKey;
                        String referenceTypeParam = referenceParam + DomainObjectDao.REFERENCE_TYPE_POSTFIX;

                        ReferenceValue refValue = (ReferenceValue) criterionValue;

                        long refId = ((RdbmsId) refValue.get()).getId();
                        long refTypeId = ((RdbmsId) refValue.get()).getTypeId();

                        parameters.put(referenceParam, refId);
                        parameters.put(referenceTypeParam, refTypeId);
                    } else if (criterionValue instanceof ListValue) {
                        ListValue listValue = (ListValue) criterionValue;
                        int index = 0;
                        for (Value value : listValue.getValues()) {
                            ReferenceValue refValue = ReferenceFilterUtility.getReferenceValue(value);
                            if (refValue == null) {
                                continue;
                            }
                            String referenceParam = filterValue.getFilter() + "_" + criterionKey + "_" + index;

                            addParametersForReference(parameters, refValue, referenceParam);
                            index++;
                        }
                    }
                }
            }
        }
    }

    private IdentifiableObjectCollection getCollectionFromGenerator(String collectionGeneratorComponent, List<? extends Filter> filterValues,
            SortOrder sortOrder, int offset, int limit) {
        CollectionDataGenerator collectionDataGenerator = (CollectionDataGenerator) serverComponentService.getServerComponent(collectionGeneratorComponent);
        return collectionDataGenerator.findCollection(filterValues, sortOrder, offset, limit);
    }

    /**
     * При запросе коллекции с набором фильтров, если в этом наборе дважды встречается фильтр с одним и тем же именем,
     * следует выбрасывать исключение IllegalArgumentException.
     * @param filterValues набор фильтров
     */
    private void checkFilterValues(List<? extends Filter> filterValues) {
        if (filterValues != null && !filterValues.isEmpty()) {
            List<String> names = new ArrayList<>();
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
        long preparationTime = System.nanoTime() - start;
        SqlLogger.SQL_PREPARATION_TIME_CACHE.set(preparationTime);

        return jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(columnToConfigMapForSelectItems, configurationExplorer, domainObjectTypeIdCache));
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public IdentifiableObjectCollection findCollectionByQuery(String query, List<? extends Value> params,
            int offset, int limit, AccessToken accessToken) {
        long start = System.nanoTime();

        Map<String, Object> parameters = new HashMap<>();

        Set<ListValue> listValueParams = getListValueParams(params);
        String collectionQuery;
        Map<String, FieldConfig> columnToConfigMapForSelectItems;
        CollectionQueryEntry cachedQueryEntry = collectionQueryCache.getCollectionQuery(query, offset, limit, listValueParams, accessToken);

        if (cachedQueryEntry != null) {
            collectionQuery = cachedQueryEntry.getQuery();
            columnToConfigMapForSelectItems = cachedQueryEntry.getColumnToConfigMap();
            addReferenceParameters(parameters, params);
        } else {
            CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);

            collectionQuery = adjustParameterNamesBeforePreProcessing(query, CollectionsDaoImpl.PARAM_NAME_PREFIX);
            collectionQuery = collectionQueryInitializer.initializeQuery(collectionQuery, offset, limit, accessToken);

            SqlQueryParser sqlParser = new SqlQueryParser(collectionQuery);
            Select select = sqlParser.getSelectStatement();

            SqlQueryModifier sqlQueryModifier = createSqlQueryModifier();

            Map<String, FieldConfig> columnToConfigMap = sqlQueryModifier.buildColumnToConfigMapForParameters(select);
            columnToConfigMapForSelectItems = sqlQueryModifier.buildColumnToConfigMapForSelectItems(select);

            // нужно объединить конфигурации колонок из Select части запроса и и для параметров для случая, когда в
            // параметре указывается алиас колонки из подзапроса
            columnToConfigMap.putAll(columnToConfigMapForSelectItems);

            collectionQuery = sqlQueryModifier.modifyQueryWithParameters(select, params, columnToConfigMap, parameters);
            collectionQuery = wrapAndLowerCaseNames(new SqlQueryParser(collectionQuery).getSelectStatement());
            collectionQuery = adjustParameterNamesAfterPreProcessing(collectionQuery);

            CollectionQueryEntry collectionQueryEntry = new CollectionQueryEntry(collectionQuery, columnToConfigMapForSelectItems);
            collectionQueryCache.putCollectionQuery(query, offset, limit, listValueParams, accessToken, collectionQueryEntry);
        }

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }
        fillParameterMap(params, parameters);

        long preparationTime = System.nanoTime() - start;
        SqlLogger.SQL_PREPARATION_TIME_CACHE.set(preparationTime);

        return jdbcTemplate.query(collectionQuery, parameters,
                new CollectionRowMapper(columnToConfigMapForSelectItems, configurationExplorer, domainObjectTypeIdCache));
    }

    private Set<ListValue> getListValueParams(List<? extends Value> params) {
        Set<ListValue> listValueParams = new HashSet<>();
        for (Value value : params) {
            if (value instanceof ListValue && ((ListValue) value).getValues().size() > 1) {
                listValueParams.add((ListValue) value);
            }
        }
        return listValueParams;
    }

    private SqlQueryModifier createSqlQueryModifier() {
        return new SqlQueryModifier(configurationExplorer, userGroupCache, currentUserAccessor);
    }

    private void addReferenceParameters(Map<String, Object> parameters, List<? extends Value> params) {
        if (params == null) {
            return;
        }
        int paramIndex = 0;
        ReferenceValue referenceValue = null;
        for (Value value : params) {
            if (value instanceof ReferenceValue) {
                referenceValue = (ReferenceValue) value;
                if (referenceValue.get() != null) {
                    String referenceParam = CollectionsDaoImpl.JDBC_PARAM_PREFIX + paramIndex;
                    addParametersForReference(parameters, referenceValue, referenceParam);

                }
            } else if (value instanceof StringValue) {
                String strValue = ((StringValue) value).get();
                if (strValue != null){
                    try {
                        referenceValue = new ReferenceValue(new RdbmsId(strValue));
                    } catch (IllegalArgumentException ex) {
                        // not reference string presentation
                    }
                    if (referenceValue != null) {
                        String referenceParam = CollectionsDaoImpl.JDBC_PARAM_PREFIX + paramIndex;
                        addParametersForReference(parameters, referenceValue, referenceParam);
    
                    }
                }
            } else if (value instanceof ListValue) {
                ListValue listValue = (ListValue) value;

                int index = 0;
                for (Value inValue : listValue.getValues()) {
                    ReferenceValue refValue = null;
                    if (inValue instanceof ReferenceValue) {
                        refValue = (ReferenceValue) inValue;
                        // ссылочные параметры могут передаваться в строковом виде.
                    } else if (inValue instanceof StringValue) {
                        String strParamValue = ((StringValue) inValue).get();
                        try {
                            refValue = new ReferenceValue(new RdbmsId(strParamValue));
                        } catch (IllegalArgumentException ex) {
                            // not reference string presentation
                        }
                    }

                    if (refValue == null) {
                        continue;
                    }
                    String referenceParam = CollectionsDaoImpl.JDBC_PARAM_PREFIX + paramIndex + "_" + index;
                    addParametersForReference(parameters, refValue, referenceParam);
                    index++;
                }
            }
            paramIndex++;
        }
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
     * ru.intertrust.cm.core.dao.api.DomainObjectDao#findCollectionCountByQuery(ru.intertrust.cm.core.config.model.
     * CollectionNestedConfig , java.util.List, ru.intertrust.cm.core.business.api.dto.SortOrder)}
     */
    @Override
    public int findCollectionCount(String collectionName,
            List<? extends Filter> filterValues, AccessToken accessToken) {

        long start = System.nanoTime();

        filterValues = processIdsFilters(filterValues);

        checkFilterValues(filterValues);

        CollectionConfig collectionConfig = configurationExplorer.getConfig(CollectionConfig.class, collectionName);

        if (collectionConfig.getGenerator() != null) {
            String collectionGeneratorComponent = collectionConfig.getGenerator().getClassName();
            CollectionDataGenerator collectionDataGenerator = (CollectionDataGenerator) serverComponentService.getServerComponent(collectionGeneratorComponent);
            return collectionDataGenerator.findCollectionCount(filterValues);
        }

        String collectionQuery;
        CollectionQueryEntry cachedQueryEntry = collectionQueryCache.getCollectionCountQuery(collectionName, filterValues, accessToken);

        if (cachedQueryEntry != null) {
            collectionQuery = cachedQueryEntry.getQuery();
        } else {

            collectionQuery = getFindCollectionCountQuery(collectionConfig, filterValues, accessToken);

            collectionQuery = adjustParameterNamesForSpring(collectionQuery);
            collectionQuery = wrapAndLowerCaseNames(new SqlQueryParser(collectionQuery).getSelectStatement());
            CollectionQueryEntry collectionQueryEntry = new CollectionQueryEntry(collectionQuery, null);
            collectionQueryCache.putCollectionCountQuery(collectionName, filterValues, accessToken, collectionQueryEntry);
        }

        Map<String, Object> parameters = new HashMap<>();
        fillFilterParameters(filterValues, parameters, true);

        if (accessToken.isDeferred()) {
            fillAclParameters(accessToken, parameters);
        }
        long preparationTime = System.nanoTime() - start;
        SqlLogger.SQL_PREPARATION_TIME_CACHE.set(preparationTime);

        return jdbcTemplate.queryForObject(collectionQuery, parameters, Integer.class);
    }

    protected CollectionQueryInitializer createCollectionQueryInitializer(ConfigurationExplorer configurationExplorer) {
        return new CollectionQueryInitializerImpl(configurationExplorer, userGroupCache, currentUserAccessor);
    }

    /**
     * Возвращает запрос, который используется в методе поиска коллекции доменных объектов
     */
    protected String getFindCollectionQuery(CollectionConfig collectionConfig,
            List<? extends Filter> filterValues, SortOrder sortOrder,
            int offset, int limit, AccessToken accessToken) {
        CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);

        return collectionQueryInitializer.initializeQuery(collectionConfig, filterValues,
                sortOrder, offset, limit, accessToken);
    }

    /**
     * Возвращает запрос, который используется в методе поиска количества объектов в коллекции
     */
    protected String getFindCollectionCountQuery(CollectionConfig collectionConfig,
            List<? extends Filter> filterValues, AccessToken accessToken) {
        CollectionQueryInitializer collectionQueryInitializer = createCollectionQueryInitializer(configurationExplorer);

        return collectionQueryInitializer.initializeCountQuery(collectionConfig, filterValues, accessToken);
    }

    private void fillParameterMap(List<? extends Value> params, Map<String, Object> parameterMap) {
        int index = 0;
        for (Value value : params) {
            // ссылочные параметры не добавляются в список параметров, т.к. их значения подставляются прямо в запрос
            if (!(value instanceof ReferenceValue)) {
                // в исполняемом SQL запросе названия параметров совпадает с их номерами в начальном SQL запросе.
                String parameterName = index + "";
                setParameter(parameterName, value, parameterMap);
            }
            index++;
        }
    }

    private void fillAclParameters(AccessToken accessToken, Map<String, Object> parameters) {
        parameters.put("user_id", ((UserSubject) accessToken.getSubject()).getUserId());
    }

    /**
     * Модифицирует имена параметров в названия совместимые с {@see NamedParameterJdbcTemplate}. Заменяет префикс
     * "_PARAM_NAME_" на ":"
     * @param collectionQuery SQL запрос
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
     * Заполняет параметры. Имена параметров в формате имя_фильтра + ключ парметра, указанный в конфигурации.
     */
    private void fillFilterParameters(List<? extends Filter> filterValues, Map<String, Object> parameters, boolean fillReferenceParams) {
        if (filterValues != null) {
            for (Filter filter : filterValues) {
                for (Integer key : filter.getCriterionKeys()) {
                    String parameterName = filter.getFilter() + key;

                    Object criterion = getFilterCriterion(filter, key);

                    if (criterion == null) {
                        parameters.put(parameterName, null);
                    }
//                    //ссылочные параметры 
//                    if(!(filter instanceof IdsIncludedFilter) && !(filter instanceof IdsExcludedFilter) && criterion instanceof ReferenceValue){
//                        continue;
//                    }
                    if (criterion instanceof Value && (fillReferenceParams || !(criterion instanceof ReferenceValue))) {
                        setParameter(parameterName, (Value) criterion, parameters);
                    } else if (criterion instanceof List) {
                        List<Value> valuesList = (List) criterion;
                        List<Object> parameterValues = getParameterValues(valuesList);
                        parameters.put(parameterName, parameterValues);
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
